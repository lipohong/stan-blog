package com.stan.blog.content.service.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.beans.dto.user.EnhancedUserDetail;
import com.stan.blog.beans.entity.content.ContentAdminEntity;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.beans.repository.content.ContentAdminRepository;
import com.stan.blog.core.exception.StanBlogRuntimeException;
import com.stan.blog.core.service.NotificationService;
import com.stan.blog.core.utils.BasicConverter;
import com.stan.blog.core.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentAdminService {

    private final ContentAdminRepository contentAdminRepository;
    private final NotificationService notificationService;
    private final ContentGeneralInfoService contentGeneralInfoService;

    @Transactional
    public ContentAdminEntity createDefaultAdminRecord(String contentId) {
        ContentAdminEntity entity = new ContentAdminEntity();
        entity.setContentId(contentId);
        entity.setBanned(Boolean.FALSE);
        entity.setRecommended(Boolean.FALSE);
        entity.setReason(null);
        return contentAdminRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Optional<ContentAdminEntity> findByContentId(String contentId) {
        return contentAdminRepository.findById(contentId);
    }

    @Transactional(readOnly = true)
    public Map<String, ContentAdminEntity> findByContentIds(Collection<String> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            return Map.of();
        }
        return contentAdminRepository.findAllById(contentIds).stream()
                .collect(Collectors.toMap(ContentAdminEntity::getContentId, entity -> entity));
    }

    @Transactional
    public BaseContentDTO banContentById(String id) {
        ContentAdminEntity entity = getRequiredEntity(id);
        entity.setBanned(true);
        entity.setReason("Violating content, blocked by administrators");
        contentAdminRepository.save(entity);

        sendBanNotification(id);
        return BasicConverter.convert(entity, BaseContentDTO.class);
    }

    @Transactional
    public BaseContentDTO unbanContentById(String id) {
        ContentAdminEntity entity = getRequiredEntity(id);
        entity.setBanned(false);
        entity.setReason("Content has been reviewed and unbanned by administrators");
        contentAdminRepository.save(entity);
        return BasicConverter.convert(entity, BaseContentDTO.class);
    }

    @Transactional
    public BaseContentDTO recommmendContentById(String id) {
        ContentAdminEntity entity = getRequiredEntity(id);
        entity.setRecommended(true);
        entity.setReason("High-quality content, recommended by administrators");
        contentAdminRepository.save(entity);

        sendRecommendationNotification(id);
        return BasicConverter.convert(entity, BaseContentDTO.class);
    }

    @Transactional
    public BaseContentDTO unrecommendContentById(String id) {
        ContentAdminEntity entity = getRequiredEntity(id);
        entity.setRecommended(false);
        entity.setReason("Content recommendation has been removed by administrators");
        contentAdminRepository.save(entity);
        return BasicConverter.convert(entity, BaseContentDTO.class);
    }

    private ContentAdminEntity getRequiredEntity(String contentId) {
        return contentAdminRepository.findById(contentId)
                .orElseThrow(() -> new StanBlogRuntimeException("Content admin record not found"));
    }

    private void sendBanNotification(String contentId) {
        try {
            ContentGeneralInfoEntity content = contentGeneralInfoService.findById(contentId);
            if (content != null) {
                EnhancedUserDetail currentUser = SecurityUtil.getCurrentUserDetail();
                Long adminId = currentUser != null ? currentUser.getUserProfile().getId() : null;

                notificationService.notifyContentBanned(
                    contentId,
                    content.getTitle(),
                    content.getContentType(),
                    content.getOwnerId(),
                    adminId
                );
            }
        } catch (Exception e) {
            log.error("Failed to send ban notification for content {}", contentId, e);
        }
    }

    private void sendRecommendationNotification(String contentId) {
        try {
            ContentGeneralInfoEntity content = contentGeneralInfoService.findById(contentId);
            if (content != null) {
                EnhancedUserDetail currentUser = SecurityUtil.getCurrentUserDetail();
                Long adminId = currentUser != null ? currentUser.getUserProfile().getId() : null;

                notificationService.notifyContentRecommended(
                    contentId,
                    content.getTitle(),
                    content.getContentType(),
                    content.getOwnerId(),
                    adminId
                );
            }
        } catch (Exception e) {
            log.error("Failed to send recommendation notification for content {}", contentId, e);
        }
    }
}