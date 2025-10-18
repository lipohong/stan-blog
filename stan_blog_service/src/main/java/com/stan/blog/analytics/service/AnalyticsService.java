package com.stan.blog.analytics.service;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.stan.blog.beans.dto.analytics.UserContentAnalyticsDTO;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.beans.repository.content.ContentGeneralInfoRepository;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final ContentGeneralInfoRepository contentGeneralInfoRepository;

    public UserContentAnalyticsDTO getOverallResult(Long userId) {
        return calculateStatistics(userId, null);
    }

    public UserContentAnalyticsDTO getResult(String contentType, Long userId) {
        return calculateStatistics(userId, contentType);
    }

    private UserContentAnalyticsDTO calculateStatistics(Long userId, String contentType) {
        Specification<ContentGeneralInfoEntity> specification = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("ownerId"), userId);
            if (contentType != null && !contentType.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("contentType"), contentType));
            }
            return predicate;
        };

        List<ContentGeneralInfoEntity> contents = contentGeneralInfoRepository.findAll(specification);
        UserContentAnalyticsDTO result = new UserContentAnalyticsDTO();
        contents.forEach(content -> statistics(content, result));
        return result;
    }

    private void statistics(ContentGeneralInfoEntity content, UserContentAnalyticsDTO result) {
        result.setTotalCount(result.getTotalCount() + 1);
        Long likeCount = content.getLikeCount() == null ? 0L : content.getLikeCount();
        Long viewCount = content.getViewCount() == null ? 0L : content.getViewCount();
        result.setTotalLikeCount(result.getTotalLikeCount() + likeCount);
        result.setTotalViewCount(result.getTotalViewCount() + viewCount);
        if (Boolean.TRUE.equals(content.getPublicToAll())) {
            result.setPublicCount(result.getPublicCount() + 1);
        }
    }
}
