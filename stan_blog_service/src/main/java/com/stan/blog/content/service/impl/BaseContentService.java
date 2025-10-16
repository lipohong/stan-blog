package com.stan.blog.content.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.consts.Const;
import com.stan.blog.beans.consts.Const.Visibility;
import com.stan.blog.beans.dto.content.BaseContentCreationDTO;
import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.beans.dto.content.BaseContentUpdateDTO;
import com.stan.blog.beans.dto.content.BaseSearchFilter;
import com.stan.blog.beans.dto.content.ContentVisibilityUpdateDTO;
import com.stan.blog.beans.dto.tag.TagInfoDTO;
import com.stan.blog.beans.entity.content.BaseContentEntity;
import com.stan.blog.beans.entity.content.ContentAdminEntity;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.beans.entity.user.UserEntity;
import com.stan.blog.beans.repository.user.UserRepository;
import com.stan.blog.content.service.IContentService;
import com.stan.blog.core.exception.StanBlogRuntimeException;
import com.stan.blog.core.utils.CacheUtil;
import com.stan.blog.core.utils.SecurityUtil;
import com.stan.blog.core.utils.UserDisplayNameUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseContentService<
    D extends BaseContentDTO,
    C extends BaseContentCreationDTO,
    U extends BaseContentUpdateDTO,
    E extends BaseContentEntity>
        implements IContentService<D, C, U, E> {

    private final ContentGeneralInfoService contentGeneralInfoService;
    private final ContentAdminService contentAdminService;
    private final ContentTagService contentTagService;
    private final UserRepository userRepository;
    private final CacheUtil cacheUtil;

    protected abstract JpaRepository<E, String> getRepository();

    protected abstract D getConcreteDTO();

    protected abstract E getConcreteEntity();

    protected abstract Const.ContentType getContentType();

    @Override
    @Transactional
    public D save(C creationDTO) {
        ContentGeneralInfoEntity generalInfo = new ContentGeneralInfoEntity();
        BeanUtils.copyProperties(creationDTO, generalInfo);
        generalInfo.setContentType(getContentType().name());
        generalInfo.setDeleted(Boolean.FALSE);
        generalInfo.setPublicToAll(Boolean.FALSE);
        generalInfo.setViewCount(0L);
        generalInfo.setLikeCount(0L);
        generalInfo.setOwnerId(SecurityUtil.getUserId());
        ContentGeneralInfoEntity savedGeneralInfo = contentGeneralInfoService.save(generalInfo);

        E contentEntity = getConcreteEntity();
        BeanUtils.copyProperties(creationDTO, contentEntity);
        contentEntity.setContentId(savedGeneralInfo.getId());
        contentEntity.setDeleted(Boolean.FALSE);
        getRepository().save(contentEntity);

        contentTagService.replaceContentTags(savedGeneralInfo.getId(), creationDTO.getTags());
        contentAdminService.createDefaultAdminRecord(savedGeneralInfo.getId());

        return getDTOById(savedGeneralInfo.getId());
    }

    @Override
    @Transactional
    public D update(U updateDTO) {
        ContentGeneralInfoEntity generalInfo = contentGeneralInfoService.getAndValidateContent(updateDTO.getId());
        BeanUtils.copyProperties(updateDTO, generalInfo);
        contentGeneralInfoService.save(generalInfo);

        E contentEntity = getRepository().findById(updateDTO.getId())
                .orElseThrow(() -> new StanBlogRuntimeException("Content does not exist"));
        BeanUtils.copyProperties(updateDTO, contentEntity);
        contentEntity.setContentId(updateDTO.getId());
        getRepository().save(contentEntity);

        contentTagService.replaceContentTags(updateDTO.getId(), updateDTO.getTags());
        return getDTOById(updateDTO.getId());
    }

    @Override
    @Transactional
    public void delete(String id) {
        contentGeneralInfoService.getAndValidateContent(id);
        contentGeneralInfoService.deleteById(id);
        getRepository().deleteById(id);
        contentTagService.replaceContentTags(id, List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<D> search(int current, int size, BaseSearchFilter filter) {
        int resolvedPage = Math.max(current - 1, 0);
        int resolvedSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<ContentGeneralInfoEntity> page = contentGeneralInfoService.searchContentForOwner(
                SecurityUtil.getUserId(), getContentType(), filter, pageable);
        if (page.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<String> contentIds = page.getContent().stream().map(ContentGeneralInfoEntity::getId).toList();
        Map<String, E> contentMap = toContentMap(getRepository().findAllById(contentIds));
        Map<String, ContentAdminEntity> adminMap = contentAdminService.findByContentIds(contentIds);
        Map<String, List<TagInfoDTO>> tagsMap = contentTagService.findTagsForContents(contentIds);
        Map<Long, UserEntity> ownerMap = loadOwners(page.getContent());

        List<D> dtoList = page.getContent().stream()
                .map(info -> buildDto(info, contentMap.get(info.getId()), adminMap.get(info.getId()),
                        tagsMap.getOrDefault(info.getId(), List.of()), ownerMap.get(info.getOwnerId())))
                .toList();
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public D getDTOById(String id) {
        ContentGeneralInfoEntity generalInfo = contentGeneralInfoService.findById(id);
        if (generalInfo == null) {
            return null;
        }
        E content = getRepository().findById(id).orElse(null);
        ContentAdminEntity admin = contentAdminService.findByContentId(id).orElse(null);
        List<TagInfoDTO> tags = contentTagService.findTagsForContent(id);
        UserEntity owner = userRepository.findById(generalInfo.getOwnerId()).orElse(null);
        return buildDto(generalInfo, content, admin, tags, owner);
    }

    @Override
    @Transactional
    public D getDTOByIdAndCount(String id) {
        D dto = getDTOById(id);
        if (dto == null) {
            return null;
        }
        if (!Boolean.TRUE.equals(dto.getPublicToAll()) || Boolean.TRUE.equals(dto.getBanned())) {
            return null;
        }
        cacheUtil.hIncr(Const.CONTENT_VIEW_COUNT_KEY, id, 1L);
        dto.setViewCount(dto.getViewCount() + 1);
        return dto;
    }

    @Override
    @Transactional
    public D updateVisibility(String id, ContentVisibilityUpdateDTO updateDTO) {
        ContentGeneralInfoEntity entity = contentGeneralInfoService.getAndValidateContent(id);
        if (updateDTO.getVisibility() == Visibility.PUBLIC) {
            if (Boolean.TRUE.equals(entity.getPublicToAll())) {
                throw new StanBlogRuntimeException("Content has already been released");
            }
            entity.setPublicToAll(Boolean.TRUE);
            entity.setPublishTime(new java.sql.Timestamp(System.currentTimeMillis()));
        } else {
            if (!Boolean.TRUE.equals(entity.getPublicToAll())) {
                throw new StanBlogRuntimeException("Content has already been private");
            }
            entity.setPublicToAll(Boolean.FALSE);
        }
        contentGeneralInfoService.save(entity);
        return getDTOById(id);
    }

    private Map<String, E> toContentMap(Iterable<E> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .collect(Collectors.toMap(BaseContentEntity::getContentId, Function.identity()));
    }

    private Map<Long, UserEntity> loadOwners(List<ContentGeneralInfoEntity> infos) {
        Set<Long> ownerIds = infos.stream()
                .map(ContentGeneralInfoEntity::getOwnerId)
                .collect(Collectors.toSet());
        return userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
    }

    private D buildDto(ContentGeneralInfoEntity generalInfo, E contentEntity, ContentAdminEntity adminEntity,
                       List<TagInfoDTO> tags, UserEntity owner) {
        D dto = getConcreteDTO();
        BeanUtils.copyProperties(generalInfo, dto);
        dto.setId(generalInfo.getId());
        dto.setTags(tags);
        if (adminEntity != null) {
            dto.setBanned(adminEntity.getBanned());
            dto.setRecommended(adminEntity.getRecommended());
            dto.setReason(adminEntity.getReason());
        }
        if (owner != null) {
            dto.setOwnerName(UserDisplayNameUtil.buildDisplayName(owner));
            dto.setAvatarUrl(owner.getAvatarUrl());
        }
        if (contentEntity != null) {
            BeanUtils.copyProperties(contentEntity, dto);
        }
        return dto;
    }
}
