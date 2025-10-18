package com.stan.blog.content.service.impl;

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
        if (creationDTO.getDescription() != null && creationDTO.getDescription().length() > 1000) {
            throw new StanBlogRuntimeException("Description can not exceed 1000 characters");
        }
        if (creationDTO.getCoverImgUrl() != null && creationDTO.getCoverImgUrl().length() > 2000) {
            throw new StanBlogRuntimeException("Cover image URL can not exceed 2000 characters");
        }
        ContentGeneralInfoEntity generalInfo = new ContentGeneralInfoEntity();
        BeanUtils.copyProperties(creationDTO, generalInfo);
        generalInfo.setContentType(getContentType().name());
        generalInfo.setDeleted(Boolean.FALSE);
        generalInfo.setPublicToAll(Boolean.FALSE);
        generalInfo.setContentProtected(Boolean.FALSE);
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
        // 长度校验，避免数据库约束错误
        if (updateDTO.getDescription() != null && updateDTO.getDescription().length() > 1000) {
            throw new StanBlogRuntimeException("Description can not exceed 1000 characters");
        }
        if (updateDTO.getCoverImgUrl() != null && updateDTO.getCoverImgUrl().length() > 2000) {
            throw new StanBlogRuntimeException("Cover image URL can not exceed 2000 characters");
        }
        ContentGeneralInfoEntity generalInfo = contentGeneralInfoService.getAndValidateContent(updateDTO.getId());
        org.springframework.beans.BeanUtils.copyProperties(updateDTO, generalInfo, getNullPropertyNames(updateDTO));
        contentGeneralInfoService.save(generalInfo);

        E contentEntity = getRepository().findById(updateDTO.getId())
                .orElseThrow(() -> new StanBlogRuntimeException("Content does not exist"));
        org.springframework.beans.BeanUtils.copyProperties(updateDTO, contentEntity, getNullPropertyNames(updateDTO));
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

    private Map<String, E> toContentMap(Iterable<E> contentIterable) {
        Map<String, E> contentMap = StreamSupport.stream(contentIterable.spliterator(), false)
                .collect(Collectors.toMap(BaseContentEntity::getContentId, Function.identity()));
        return contentMap;
    }

    private Map<Long, UserEntity> loadOwners(List<ContentGeneralInfoEntity> contentList) {
        Set<Long> ownerIds = contentList.stream().map(ContentGeneralInfoEntity::getOwnerId).collect(Collectors.toSet());
        Iterable<UserEntity> users = userRepository.findAllById(ownerIds);
        Map<Long, UserEntity> userMap = StreamSupport.stream(users.spliterator(), false)
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        return userMap;
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
    private static String[] getNullPropertyNames(Object source) {
        final org.springframework.beans.BeanWrapper src = new org.springframework.beans.BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        java.util.Set<String> emptyNames = new java.util.HashSet<>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
