package com.stan.blog.core.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.consts.Const.NotificationType;
import com.stan.blog.beans.dto.content.NotificationCreateDTO;
import com.stan.blog.beans.dto.content.NotificationDTO;
import com.stan.blog.beans.dto.user.EnhancedUserDetail;
import com.stan.blog.beans.dto.user.UserGeneralDTO;
import com.stan.blog.beans.entity.content.NotificationEntity;
import com.stan.blog.beans.repository.content.NotificationRepository;
import com.stan.blog.core.utils.BasicConverter;
import com.stan.blog.core.utils.JsonMetadataUtil;
import com.stan.blog.core.utils.SecurityUtil;
import com.stan.blog.core.utils.UserDisplayNameUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    @Transactional
    public NotificationDTO createNotification(NotificationCreateDTO dto) {
        NotificationEntity entity = BasicConverter.convert(dto, NotificationEntity.class);
        entity.setDeleted(false);
        entity.setIsRead(false);

        NotificationEntity saved = notificationRepository.save(entity);
        log.info("Notification created: {} for user {}", dto.getNotificationType(), dto.getRecipientId());
        return convertToDTO(saved);
    }

    public Page<NotificationDTO> getUserNotifications(Long userId, int page, int size, Boolean isRead) {
        Pageable pageable = resolvePageable(page, size);
        Page<NotificationEntity> notificationPage;
        if (isRead != null) {
            notificationPage = notificationRepository.findByRecipientIdAndDeletedFalseAndIsRead(userId, isRead, pageable);
        } else {
            notificationPage = notificationRepository.findByRecipientIdAndDeletedFalse(userId, pageable);
        }
        List<NotificationDTO> notifications = notificationPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(notifications, pageable, notificationPage.getTotalElements());
    }

    @Transactional
    public boolean markAsRead(Long notificationId, Long userId) {
        return notificationRepository.findById(notificationId)
                .filter(notification -> notification.getRecipientId().equals(userId))
                .map(notification -> {
                    notification.setIsRead(true);
                    notificationRepository.save(notification);
                    log.info("Notification {} marked as read by user {}", notificationId, userId);
                    return true;
                })
                .orElseGet(() -> {
                    log.warn("Notification {} not found or access denied for user {}", notificationId, userId);
                    return false;
                });
    }

    @Transactional
    public int markAllAsRead(Long userId) {
        List<NotificationEntity> unreadNotifications = notificationRepository
                .findByRecipientIdAndDeletedFalseAndIsReadFalse(userId);
        if (unreadNotifications.isEmpty()) {
            return 0;
        }
        unreadNotifications.forEach(notification -> notification.setIsRead(true));
        notificationRepository.saveAll(unreadNotifications);
        int updatedCount = unreadNotifications.size();
        log.info("Marked {} notifications as read for user {}", updatedCount, userId);
        return updatedCount;
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndDeletedFalseAndIsReadFalse(userId);
    }

    public long getCurrentUserUnreadCount() {
        EnhancedUserDetail currentUser = SecurityUtil.getCurrentUserDetail();
        if (currentUser == null) {
            return 0;
        }
        return getUnreadCount(currentUser.getUserProfile().getId());
    }

    public void notifyContentRecommended(String contentId, String contentTitle, String contentType, Long recipientId, Long senderId) {
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(recipientId);
        dto.setSenderId(senderId);
        dto.setNotificationType(NotificationType.CONTENT_RECOMMENDED);
        dto.setTitle("notifications.messages.content-recommended.title");
        dto.setMessage("notifications.messages.content-recommended.message");
        dto.setRelatedContentId(contentId);
        dto.setRelatedContentType(contentType);
        dto.setRelatedContentTitle(contentTitle);
        dto.setActionUrl("/content/" + contentId);
        dto.setMetadata(JsonMetadataUtil.createNotificationI18nMetadata(contentType, contentTitle));
        createNotification(dto);
    }

    public void notifyContentCommented(String contentId, String contentTitle, String contentType, Long contentOwnerId,
            Long commenterId, String commenterName, String commentContent) {
        if (contentOwnerId.equals(commenterId)) {
            return;
        }
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(contentOwnerId);
        dto.setSenderId(commenterId);
        dto.setNotificationType(NotificationType.CONTENT_COMMENTED);
        dto.setTitle("notifications.messages.content-commented.title");
        dto.setMessage("notifications.messages.content-commented.message");
        dto.setRelatedContentId(contentId);
        dto.setRelatedContentType(contentType);
        dto.setRelatedContentTitle(contentTitle);
        dto.setActionUrl("/content/" + contentId);
        dto.setMetadata(JsonMetadataUtil.createCommentNotificationI18nMetadata(commenterName, contentType.toLowerCase(), contentTitle, commentContent));
        createNotification(dto);
    }

    public void notifyContentBanned(String contentId, String contentTitle, String contentType, Long contentOwnerId, Long adminId) {
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(contentOwnerId);
        dto.setSenderId(adminId);
        dto.setNotificationType(NotificationType.CONTENT_BANNED);
        dto.setTitle("notifications.messages.content-banned.title");
        dto.setMessage("notifications.messages.content-banned.message");
        dto.setRelatedContentId(contentId);
        dto.setRelatedContentType(contentType);
        dto.setRelatedContentTitle(contentTitle);
        dto.setActionUrl("/content/" + contentId);
        
        // Store parameters for i18n resolution
        dto.setMetadata(JsonMetadataUtil.createNotificationI18nMetadata(contentType.toLowerCase(), contentTitle));
        
        createNotification(dto);
    }

    public void notifyReplyCommented(String contentId, String contentTitle, String contentType, Long originalCommenterId,
            Long replyerId, String replierName, String replyContent, String originalCommentContent) {
        if (originalCommenterId.equals(replyerId)) {
            return;
        }
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(originalCommenterId);
        dto.setSenderId(replyerId);
        dto.setNotificationType(NotificationType.COMMENT_REPLIED);
        dto.setTitle("notifications.messages.comment-replied.title");
        dto.setMessage("notifications.messages.comment-replied.message");
        dto.setRelatedContentId(contentId);
        dto.setRelatedContentType(contentType);
        dto.setRelatedContentTitle(contentTitle);
        dto.setActionUrl("/content/" + contentId);
        dto.setMetadata(JsonMetadataUtil.createReplyNotificationI18nMetadata(replierName, contentTitle, replyContent,
                originalCommentContent));
        createNotification(dto);
    }

    public void notifyCommentDeleted(String contentId, String contentTitle, String contentType, Long contentOwnerId,
            Long deleterId, String deleterName, String deletedCommentContent) {
        if (contentOwnerId.equals(deleterId)) {
            return;
        }
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(contentOwnerId);
        dto.setSenderId(deleterId);
        dto.setNotificationType(NotificationType.COMMENT_DELETED);
        dto.setTitle("notifications.messages.comment-deleted.title");
        dto.setMessage("notifications.messages.comment-deleted.message");
        dto.setRelatedContentId(contentId);
        dto.setRelatedContentType(contentType);
        dto.setRelatedContentTitle(contentTitle);
        dto.setActionUrl("/content/" + contentId);
        dto.setMetadata(JsonMetadataUtil.createDeletedCommentNotificationI18nMetadata(contentType.toLowerCase(),
                contentTitle, deletedCommentContent));
        createNotification(dto);
    }

    public void notifyReplyDeleted(String contentId, String contentTitle, String contentType, Long originalCommenterId,
            Long deleterId, String deleterName, String deletedReplyContent, String originalCommentContent) {
        if (originalCommenterId.equals(deleterId)) {
            return;
        }
        NotificationCreateDTO dto = new NotificationCreateDTO();
        dto.setRecipientId(originalCommenterId);
        dto.setSenderId(deleterId);
        dto.setNotificationType(NotificationType.REPLY_DELETED);
        dto.setTitle("notifications.messages.reply-deleted.title");
        dto.setMessage("notifications.messages.reply-deleted.message");
        dto.setRelatedContentId(contentId);
        dto.setRelatedContentType(contentType);
        dto.setRelatedContentTitle(contentTitle);
        dto.setActionUrl("/content/" + contentId);
        dto.setMetadata(JsonMetadataUtil.createDeletedReplyNotificationI18nMetadata(contentTitle, deletedReplyContent,
                originalCommentContent));
        createNotification(dto);
    }

    private Pageable resolvePageable(int page, int size) {
        int resolvedPage = Math.max(page - 1, 0);
        int resolvedSize = Math.max(size, 1);
        return PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createTime"));
    }

    private NotificationDTO convertToDTO(NotificationEntity entity) {
        NotificationDTO dto = BasicConverter.convert(entity, NotificationDTO.class);
        if (entity.getSenderId() != null) {
            UserGeneralDTO sender = userService.getUser(entity.getSenderId());
            if (sender != null) {
                dto.setSenderName(UserDisplayNameUtil.buildDisplayName(sender));
            }
        }
        return dto;
    }
}