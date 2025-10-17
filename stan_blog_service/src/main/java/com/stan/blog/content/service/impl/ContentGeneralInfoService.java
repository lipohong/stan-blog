package com.stan.blog.content.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.consts.Const;
import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.beans.dto.content.BaseSearchFilter;
import com.stan.blog.beans.dto.tag.TagInfoDTO;
import com.stan.blog.beans.entity.content.ContentAdminEntity;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.beans.entity.user.UserEntity;
import com.stan.blog.beans.repository.content.ContentAdminRepository;
import com.stan.blog.beans.repository.content.ContentGeneralInfoRepository;
import com.stan.blog.beans.repository.user.UserRepository;
import com.stan.blog.core.exception.StanBlogRuntimeException;
import com.stan.blog.core.utils.SecurityUtil;
import com.stan.blog.core.utils.UserDisplayNameUtil;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContentGeneralInfoService {

    private final ContentGeneralInfoRepository contentGeneralInfoRepository;
    private final ContentAdminRepository contentAdminRepository;
    private final ContentTagService contentTagService;
    private final UserRepository userRepository;

    @Transactional
    public void sinkViewCountToDB(String typeKey, String id, Long increasedCount) {
        if (StringUtils.isBlank(id) || increasedCount == null || increasedCount <= 0) {
            return;
        }
        ContentGeneralInfoEntity entity = findById(id);
        if (entity == null) {
            return;
        }
        switch (typeKey) {
            case Const.CONTENT_VIEW_COUNT_KEY -> entity.setViewCount(
                    (entity.getViewCount() == null ? 0L : entity.getViewCount()) + increasedCount);
            case Const.CONTENT_LIKE_COUNT_KEY -> entity.setLikeCount(
                    (entity.getLikeCount() == null ? 0L : entity.getLikeCount()) + increasedCount);
            default -> {
                return;
            }
        }
        contentGeneralInfoRepository.save(entity);
    }

    @Transactional
    public ContentGeneralInfoEntity save(ContentGeneralInfoEntity entity) {
        return contentGeneralInfoRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public ContentGeneralInfoEntity findById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        Optional<ContentGeneralInfoEntity> optional = contentGeneralInfoRepository.findById(id);
        return optional.orElse(null);
    }

    @Transactional
    public void deleteById(String id) {
        contentGeneralInfoRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ContentGeneralInfoEntity> searchContentForOwner(Long ownerId, Const.ContentType contentType,
            BaseSearchFilter filter, Pageable pageable) {
        Specification<ContentGeneralInfoEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("ownerId"), ownerId));
            predicates.add(cb.equal(root.get("contentType"), contentType.name()));

            if (filter != null) {
                if (StringUtils.isNotBlank(filter.getStatus())) {
                    boolean isPublished = "PUBLISHED".equalsIgnoreCase(filter.getStatus());
                    boolean isDraft = "DRAFT".equalsIgnoreCase(filter.getStatus());
                    if (isPublished) {
                        predicates.add(cb.isTrue(root.get("publicToAll")));
                    } else if (isDraft) {
                        predicates.add(cb.isFalse(root.get("publicToAll")));
                    }
                }
                if (filter.getTopic() != null) {
                    predicates.add(cb.equal(root.get("topic"), filter.getTopic()));
                }
                if (filter.getCreateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("createTime"), filter.getCreateFrom()));
                }
                if (filter.getCreateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("createTime"), filter.getCreateTo()));
                }
                if (filter.getUpdateFrom() != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("updateTime"), filter.getUpdateFrom()));
                }
                if (filter.getUpdateTo() != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("updateTime"), filter.getUpdateTo()));
                }
                if (StringUtils.isNotBlank(filter.getKeyword())) {
                    String keyword = "%" + filter.getKeyword().toLowerCase() + "%";
                    predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), keyword),
                        cb.like(cb.lower(root.get("description")), keyword)
                    ));
                }
                if (filter.getTags() != null && filter.getTags().length > 0) {
                    Set<Long> tagIds = new HashSet<>();
                    for (String tagId : filter.getTags()) {
                        if (StringUtils.isNumeric(tagId)) {
                            tagIds.add(Long.valueOf(tagId));
                        }
                    }
                    if (!tagIds.isEmpty()) {
                        Set<String> contentIds = contentTagService.findContentIdsWithTags(tagIds);
                        if (contentIds.isEmpty()) {
                            return cb.disjunction();
                        }
                        predicates.add(root.get("id").in(contentIds));
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return contentGeneralInfoRepository.findAll(specification, pageable);
    }

    @Transactional(readOnly = true)
    public Page<BaseContentDTO> searchPublishedContentDTOs(int current, int size, String keyword, String status, String topic) {
        int resolvedPage = Math.max(current - 1, 0);
        int resolvedSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Specification<ContentGeneralInfoEntity> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("publicToAll")));
            predicates.add(cb.isFalse(root.get("deleted")));

            if (StringUtils.isNotBlank(keyword)) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)));
            }
            if (StringUtils.isNotBlank(topic)) {
                try {
                    predicates.add(cb.equal(root.get("topic"), Const.Topic.valueOf(topic)));
                } catch (IllegalArgumentException ex) {
                    return cb.disjunction();
                }
            }

            if (StringUtils.isNotBlank(status)) {
                Set<String> contentIds = switch (status.toLowerCase()) {
                    case "banned" -> contentAdminRepository.findByBannedTrue().stream()
                            .map(ContentAdminEntity::getContentId)
                            .collect(Collectors.toSet());
                    case "recommended" -> contentAdminRepository.findByRecommendedTrue().stream()
                            .map(ContentAdminEntity::getContentId)
                            .collect(Collectors.toSet());
                    default -> Collections.emptySet();
                };
                if (!contentIds.isEmpty()) {
                    predicates.add(root.get("id").in(contentIds));
                } else if (status.equalsIgnoreCase("banned") || status.equalsIgnoreCase("recommended")) {
                    return cb.disjunction();
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<ContentGeneralInfoEntity> page = contentGeneralInfoRepository.findAll(specification, pageable);
        if (page.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        List<BaseContentDTO> dtoList = mapToBaseContentDTOs(page.getContent());
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }

    protected ContentGeneralInfoEntity getAndValidateContent(String id) {
        final ContentGeneralInfoEntity entity = findById(id);
        validateExistence(entity);
        validateOwnership(entity);
        return entity;
    }

    private void validateExistence(ContentGeneralInfoEntity entity) {
        if (Objects.isNull(entity)) {
            throw new StanBlogRuntimeException("The content doesn't exists, or already has been deleted");
        }
    }

    private void validateOwnership(ContentGeneralInfoEntity entity) {
        if (entity.getOwnerId() != SecurityUtil.getUserId().longValue()) {
            throw new StanBlogRuntimeException("You can't operate a content doesn't belong to you");
        }
    }

    private List<BaseContentDTO> mapToBaseContentDTOs(List<ContentGeneralInfoEntity> entities) {
        List<String> contentIds = entities.stream().map(ContentGeneralInfoEntity::getId).toList();
        Map<String, ContentAdminEntity> adminMap = contentAdminRepository.findAllById(contentIds).stream()
                .collect(Collectors.toMap(ContentAdminEntity::getContentId, Function.identity(), (a, b) -> a));
        Map<String, List<TagInfoDTO>> tagsMap = contentTagService.findTagsForContents(contentIds);
        Set<Long> ownerIds = entities.stream().map(ContentGeneralInfoEntity::getOwnerId).collect(Collectors.toSet());
        Map<Long, UserEntity> userMap = userRepository.findAllById(ownerIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        List<BaseContentDTO> result = new ArrayList<>(entities.size());
        for (ContentGeneralInfoEntity entity : entities) {
            BaseContentDTO dto = new BaseContentDTO();
            dto.setId(entity.getId());
            dto.setTitle(entity.getTitle());
            dto.setDescription(entity.getDescription());
            dto.setCoverImgUrl(entity.getCoverImgUrl());
            dto.setPublicToAll(entity.getPublicToAll());
            dto.setPublishTime(entity.getPublishTime());
            dto.setViewCount(entity.getViewCount());
            dto.setLikeCount(entity.getLikeCount());
            dto.setOwnerId(entity.getOwnerId());
            dto.setContentType(entity.getContentType());
            dto.setContentProtected(entity.getContentProtected());
            dto.setTopic(entity.getTopic());
            dto.setCreateTime(entity.getCreateTime());
            dto.setUpdateTime(entity.getUpdateTime());

            UserEntity owner = userMap.get(entity.getOwnerId());
            if (owner != null) {
                dto.setOwnerName(UserDisplayNameUtil.buildDisplayName(owner));
                dto.setAvatarUrl(owner.getAvatarUrl());
            }

            ContentAdminEntity admin = adminMap.get(entity.getId());
            if (admin != null) {
                dto.setBanned(admin.getBanned());
                dto.setRecommended(admin.getRecommended());
                dto.setReason(admin.getReason());
            }

            dto.setTags(tagsMap.getOrDefault(entity.getId(), List.of()));
            result.add(dto);
        }
        return result;
    }
}