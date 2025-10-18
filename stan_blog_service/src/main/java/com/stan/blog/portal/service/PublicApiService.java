package com.stan.blog.portal.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
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

import com.stan.blog.beans.consts.Const;
import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.beans.dto.content.ContentBasicInfoDTO;
import com.stan.blog.beans.dto.tag.TagInfoDTO;
import com.stan.blog.beans.dto.tag.TagInfoStatisticDTO;
import com.stan.blog.beans.entity.content.ContentAdminEntity;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.beans.entity.user.UserEntity;
import com.stan.blog.beans.repository.content.ContentAdminRepository;
import com.stan.blog.beans.repository.content.ContentGeneralInfoRepository;
import com.stan.blog.beans.repository.user.UserRepository;
import com.stan.blog.content.service.impl.ContentTagService;
import com.stan.blog.core.utils.CacheUtil;
import com.stan.blog.core.utils.UserDisplayNameUtil;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PublicApiService {

    private final ContentGeneralInfoRepository contentGeneralInfoRepository;
    private final ContentAdminRepository contentAdminRepository;
    private final ContentTagService contentTagService;
    private final UserRepository userRepository;
    private final CacheUtil cacheUtil;
    private static final String LIKE_COUNT_UNI_KEY = "LIKE_COUNT_UNI_KEY";

    /**
     * The core search method for the portal home page to display users' contents
     * @param current page number (1-based)
     * @param size page size
     * @param tags tag ids in array, ignore the filter when array is empty
     * @param ownerId ignore the filter when not given
     * @param contentTypes ARTICLE, PLAN, VOCABULARY or COLLECTION
     * @return A page of user contents
     */
    public Page<BaseContentDTO> searchContents(int current, int size, String[] tags, Long ownerId,
            String[] contentTypes, Const.Topic topic, String keyword) {
        int pageIndex = Math.max(0, current - 1);
        int pageSize = Math.max(1, size);
        Pageable pageable = PageRequest.of(pageIndex, pageSize,
                Sort.by(Sort.Order.desc("createTime"), Sort.Order.desc("viewCount"), Sort.Order.desc("updateTime")));

        Set<Long> tagIdFilter = sanitizeTagIds(tags);
        Set<String> contentIdFilter = null;
        if (!tagIdFilter.isEmpty()) {
            Set<String> contentIds = contentTagService.findContentIdsWithTags(tagIdFilter);
            if (contentIds.isEmpty()) {
                return new PageImpl<>(List.of(), pageable, 0);
            }
            contentIdFilter = contentIds;
        }

        Set<String> bannedContentIds = findBannedContentIds();
        List<String> contentTypeFilter = sanitizeContentTypes(contentTypes);

        Specification<ContentGeneralInfoEntity> specification = buildSearchSpecification(contentIdFilter, ownerId,
                contentTypeFilter, topic, keyword, bannedContentIds);

        Page<ContentGeneralInfoEntity> entitiesPage = contentGeneralInfoRepository.findAll(specification, pageable);
        if (entitiesPage.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, entitiesPage.getTotalElements());
        }

        List<BaseContentDTO> dtoList = mapToBaseContentDTOs(entitiesPage.getContent());
        return new PageImpl<>(dtoList, pageable, entitiesPage.getTotalElements());
    }

    /**
     * Like a content
     * @param ip The IP address of the user
     * @param contentId The ID of the content to like
     * @return true if the content was liked, false otherwise
     */
    public Boolean likeContent(String ip, String contentId) {
        final String key = String.format("%s:%s:%s", LIKE_COUNT_UNI_KEY, ip, contentId);
        if (cacheUtil.hasKey(key)) {
            return Boolean.FALSE;
        }
        cacheUtil.hIncr(Const.CONTENT_LIKE_COUNT_KEY, contentId, 1L);
        cacheUtil.set(key, null, TimeUnit.MINUTES.toSeconds(5L));
        return Boolean.TRUE;
    }

    /**
     * Get tag info statistics
     * @param ownerId The ID of the owner
     * @param contentTypes The types of content
     * @param topic The topic of the content
     * @param keyword The keyword to search for
     * @return A list of tag info statistics
     */
    public List<TagInfoStatisticDTO> getTagInfoStatistics(Long ownerId, String[] contentTypes, Const.Topic topic,
            String keyword) {
        List<String> contentTypeFilter = sanitizeContentTypes(contentTypes);
        Set<String> bannedContentIds = findBannedContentIds();
        Specification<ContentGeneralInfoEntity> specification = buildSearchSpecification(null, ownerId,
                contentTypeFilter, topic, keyword, bannedContentIds);

        List<ContentGeneralInfoEntity> contents = contentGeneralInfoRepository.findAll(specification);
        if (contents.isEmpty()) {
            return List.of();
        }

        List<String> contentIds = contents.stream()
                .map(ContentGeneralInfoEntity::getId)
                .filter(Objects::nonNull)
                .toList();
        if (contentIds.isEmpty()) {
            return List.of();
        }

        Map<String, List<TagInfoDTO>> tagsMap = contentTagService.findTagsForContents(contentIds);
        if (tagsMap.isEmpty()) {
            return List.of();
        }

        Map<Long, TagInfoStatisticDTO> stats = new HashMap<>();
        for (List<TagInfoDTO> tagList : tagsMap.values()) {
            for (TagInfoDTO tag : tagList) {
                if (tag == null || tag.getValue() == null) {
                    continue;
                }
                TagInfoStatisticDTO stat = stats.computeIfAbsent(tag.getValue(), id -> {
                    TagInfoStatisticDTO dto = new TagInfoStatisticDTO();
                    dto.setValue(tag.getValue());
                    dto.setLabel(tag.getLabel());
                    dto.setCount(0);
                    return dto;
                });
                stat.setCount(stat.getCount() + 1);
            }
        }

        if (stats.isEmpty()) {
            return List.of();
        }

        return stats.values().stream()
                .sorted(Comparator.comparingInt(TagInfoStatisticDTO::getCount).reversed()
                        .thenComparing(dto -> StringUtils.defaultString(dto.getLabel()), Comparator.reverseOrder()))
                .toList();
    }

    /**
     * Get content basic info by ID
     * @param contentId The ID of the content
     * @return ContentBasicInfoDTO containing basic info for content redirection
     */
    public ContentBasicInfoDTO getContentBasicInfo(String contentId) {
        if (StringUtils.isBlank(contentId)) {
            return null;
        }
        ContentGeneralInfoEntity generalInfo = contentGeneralInfoRepository.findById(contentId).orElse(null);
        if (generalInfo == null) {
            return null;
        }
        ContentAdminEntity adminInfo = contentAdminRepository.findById(contentId).orElse(null);
        boolean banned = adminInfo != null && Boolean.TRUE.equals(adminInfo.getBanned());
        return new ContentBasicInfoDTO(generalInfo.getId(), generalInfo.getContentType(),
                Boolean.TRUE.equals(generalInfo.getPublicToAll()), banned);
    }

    private Specification<ContentGeneralInfoEntity> buildSearchSpecification(Set<String> contentIdFilter, Long ownerId,
            List<String> contentTypes, Const.Topic topic, String keyword, Set<String> bannedContentIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("publicToAll")));

            if (contentIdFilter != null && !contentIdFilter.isEmpty()) {
                predicates.add(root.get("id").in(contentIdFilter));
            }
            if (ownerId != null) {
                predicates.add(cb.equal(root.get("ownerId"), ownerId));
            }
            if (contentTypes != null && !contentTypes.isEmpty()) {
                predicates.add(root.get("contentType").in(contentTypes));
            }
            if (topic != null) {
                predicates.add(cb.equal(root.get("topic"), topic));
            }
            if (StringUtils.isNotBlank(keyword)) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)));
            }
            if (bannedContentIds != null && !bannedContentIds.isEmpty()) {
                predicates.add(cb.not(root.get("id").in(bannedContentIds)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private List<BaseContentDTO> mapToBaseContentDTOs(List<ContentGeneralInfoEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        List<String> contentIds = entities.stream()
                .map(ContentGeneralInfoEntity::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<String, ContentAdminEntity> adminMap = contentAdminRepository.findAllById(contentIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(ContentAdminEntity::getContentId, Function.identity(), (a, b) -> a));
        Map<String, List<TagInfoDTO>> tagsMap = contentTagService.findTagsForContents(contentIds);
        Set<Long> ownerIds = entities.stream()
                .map(ContentGeneralInfoEntity::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, UserEntity> userMap = userRepository.findAllById(ownerIds).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (a, b) -> a));

        List<BaseContentDTO> result = new ArrayList<>(entities.size());
        for (ContentGeneralInfoEntity entity : entities) {
            if (entity == null) {
                continue;
            }
            BaseContentDTO dto = new BaseContentDTO();
            dto.setId(entity.getId());
            dto.setTitle(entity.getTitle());
            dto.setDescription(entity.getDescription());
            dto.setCoverImgUrl(entity.getCoverImgUrl());
            dto.setPublicToAll(Boolean.TRUE.equals(entity.getPublicToAll()));
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
                dto.setBanned(Boolean.TRUE.equals(admin.getBanned()));
                dto.setRecommended(Boolean.TRUE.equals(admin.getRecommended()));
                dto.setReason(admin.getReason());
            } else {
                dto.setBanned(Boolean.FALSE);
                dto.setRecommended(Boolean.FALSE);
            }

            List<TagInfoDTO> tagList = tagsMap.getOrDefault(entity.getId(), Collections.emptyList());
            dto.setTags(tagList);
            result.add(dto);
        }
        return result;
    }

    private List<String> sanitizeContentTypes(String[] contentTypes) {
        if (contentTypes == null || contentTypes.length == 0) {
            return List.of();
        }
        return Arrays.stream(contentTypes)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .toList();
    }

    private Set<Long> sanitizeTagIds(String[] tags) {
        if (tags == null || tags.length == 0) {
            return Set.of();
        }
        Set<Long> result = new HashSet<>();
        for (String tag : tags) {
            if (StringUtils.isBlank(tag)) {
                continue;
            }
            try {
                result.add(Long.valueOf(tag.trim()));
            } catch (NumberFormatException ex) {
                log.warn("Ignoring invalid tag id: {}", tag);
            }
        }
        return result;
    }

    private Set<String> findBannedContentIds() {
        return contentAdminRepository.findByBannedTrue().stream()
                .map(ContentAdminEntity::getContentId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }
}
