package com.stan.blog.content.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.dto.content.CommentCreateDTO;
import com.stan.blog.beans.dto.content.CommentDTO;
import com.stan.blog.beans.dto.user.EnhancedUserDetail;
import com.stan.blog.beans.entity.content.CommentEntity;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.beans.repository.content.CommentRepository;
import com.stan.blog.content.service.impl.ContentGeneralInfoService;
import com.stan.blog.core.service.NotificationService;
import com.stan.blog.core.utils.BasicConverter;
import com.stan.blog.core.utils.RequestContextUtil;
import com.stan.blog.core.utils.UserDisplayNameUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final CommentRepository commentRepository;
    private final NotificationService notificationService;
    private final ContentGeneralInfoService contentGeneralInfoService;

    /**
     * Create a new comment
     * @param dto comment creation data
     * @param currentUser authenticated user
     * @return created comment DTO
     */
    @Transactional
    public CommentDTO createComment(CommentCreateDTO dto, EnhancedUserDetail currentUser) {
        CommentEntity entity = new CommentEntity();
        entity.setContentId(dto.getContentId());
        entity.setContentType(dto.getContentType());
        entity.setContent(dto.getContent());
        entity.setParentId(dto.getParentId());
        entity.setUserId(currentUser.getUserProfile().getId());

        String userName = UserDisplayNameUtil.buildDisplayName(currentUser.getUserProfile());
        entity.setUserName(userName);
        entity.setUserAvatarUrl(currentUser.getUserProfile().getAvatarUrl());
        entity.setIpAddress(RequestContextUtil.getClientIpAddress());
        entity.setDeleted(Boolean.FALSE);
        entity.setLikeCount(0L);

        // Handle reply information for quoted display
        if (dto.getParentId() != null) {
            commentRepository.findById(dto.getParentId())
                .filter(parentComment -> !Boolean.TRUE.equals(parentComment.getDeleted()))
                .ifPresent(parentComment -> {
                    entity.setReplyToUserName(parentComment.getUserName());
                    String contentSnippet = parentComment.getContent();
                    if (contentSnippet != null && contentSnippet.length() > 100) {
                        contentSnippet = contentSnippet.substring(0, 100) + "...";
                    }
                    entity.setReplyToContent(contentSnippet);
                });
        }

        CommentEntity savedComment = commentRepository.save(entity);

        if (dto.getParentId() != null) {
            // Send notification to original comment author if this is a reply
            sendCommentReplyNotification(
                dto.getContentId(),
                dto.getContentType(),
                dto.getParentId(),
                currentUser.getUserProfile().getId(),
                userName,
                dto.getContent());
        } else {
            // Send notification to content owner
            sendCommentNotification(
                dto.getContentId(),
                dto.getContentType(),
                currentUser.getUserProfile().getId(),
                userName,
                dto.getContent());
        }

        log.info("Comment created successfully for content {} by user {}",
                dto.getContentId(), currentUser.getUsername());

        return convertToDTO(savedComment);
    }

    private void sendCommentNotification(String contentId, String contentType, Long commenterId, String commenterName, String commentContent) {
        try {
            ContentGeneralInfoEntity content = contentGeneralInfoService.findById(contentId);
            if (content != null) {
                notificationService.notifyContentCommented(
                    contentId,
                    content.getTitle(),
                    contentType,
                    content.getOwnerId(),
                    commenterId,
                    commenterName,
                    commentContent
                );
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
            log.error("Failed to send comment notification for content {}", contentId, e);
        }
    }

    private void sendCommentReplyNotification(String contentId, String contentType, Long parentCommentId, Long replierId, String replierName, String replyContent) {
        try {
            ContentGeneralInfoEntity content = contentGeneralInfoService.findById(contentId);
            if (content == null) {
                return;
            }

            commentRepository.findById(parentCommentId)
                .filter(parentComment -> !Boolean.TRUE.equals(parentComment.getDeleted()))
                .ifPresent(parentComment -> notificationService.notifyCommentReplied(
                    contentId,
                    content.getTitle(),
                    contentType,
                    parentComment.getUserId(), // original comment author ID
                    replierId,
                    replierName,
                    replyContent,
                    parentComment.getContent() // original comment content
                ));
        } catch (Exception e) {
            // Log error but don't fail the main operation
            log.error("Failed to send comment reply notification for content {}", contentId, e);
        }
    }

    /**
     * Get comments for a specific content with pagination (flat structure)
     * @param contentId content ID
     * @param contentType content type
     * @param page page number (1-based)
     * @param size page size
     * @return paginated comments in flat structure
     */
    @Transactional(readOnly = true)
    public Page<CommentDTO> getCommentsForContent(
        String contentId,
        String contentType,
        int page,
        int size
    ) {
        int resolvedPage = Math.max(page - 1, 0);
        int resolvedSize = size > 0 ? size : DEFAULT_PAGE_SIZE;
        Pageable pageable = PageRequest.of(resolvedPage, resolvedSize, Sort.by(Sort.Direction.DESC, "createTime"));

        Page<CommentEntity> commentsPage = commentRepository.findByContentIdAndContentType(contentId, contentType, pageable);
        return commentsPage.map(this::convertToDTO);
    }

    /**
     * Delete a comment (soft delete)
     * @param commentId comment ID
     * @param currentUser authenticated user
     * @return true if deleted successfully
     */
    @Transactional
    public boolean deleteComment(Long commentId, EnhancedUserDetail currentUser) {
        CommentEntity comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            log.warn("Comment {} not found", commentId);
            return false;
        }

        log.info("Before deletion - Comment {} deleted status: {}", commentId, comment.getDeleted());

        // Check if current user is the owner of the comment or has admin privileges
        if (!comment.getUserId().equals(currentUser.getUserProfile().getId()) &&
            !hasAdminPrivileges(currentUser)) {
            log.warn("User {} attempted to delete comment {} without permission",
                    currentUser.getUsername(), commentId);
            return false;
        }

        // Store comment information before deletion for notifications
        String contentId = comment.getContentId();
        String contentType = comment.getContentType();
        String deletedCommentContent = comment.getContent();
        Long commentAuthorId = comment.getUserId();
        Long parentCommentId = comment.getParentId();
        String deleterName = UserDisplayNameUtil.buildDisplayName(currentUser.getUserProfile());
        Long deleterId = currentUser.getUserProfile().getId();

        try {
            commentRepository.delete(comment);
            log.info("Comment {} deleted by user {}", commentId, currentUser.getUsername());

            // Send notifications after successful deletion
            sendCommentDeletionNotifications(contentId, contentType, deletedCommentContent,
                                           commentAuthorId, parentCommentId, deleterId, deleterName);
            return true;
        } catch (Exception ex) {
            log.error("Failed to logically delete comment {} from database", commentId, ex);
            return false;
        }
    }

    /**
     * Send appropriate notifications when a comment is deleted
     * @param contentId content ID
     * @param contentType content type
     * @param deletedCommentContent deleted comment content
     * @param commentAuthorId original comment author ID
     * @param parentCommentId parent comment ID (null for top-level comments)
     * @param deleterId ID of user who deleted the comment
     * @param deleterName name of user who deleted the comment
     */
    private void sendCommentDeletionNotifications(String contentId, String contentType, String deletedCommentContent,
                                                 Long commentAuthorId, Long parentCommentId, Long deleterId, String deleterName) {
        try {
            ContentGeneralInfoEntity content = contentGeneralInfoService.findById(contentId);
            if (content == null) {
                log.warn("Content {} not found when sending deletion notifications", contentId);
                return;
            }

            // Check if content is deleted
            if (content.getDeleted() != null && content.getDeleted()) {
                log.info("Content {} is deleted - skipping deletion notification", contentId);
                return;
            }

            if (parentCommentId != null) {
                // This was a reply - handle reply deletion notifications
                handleReplyDeletionNotifications(contentId, content, deletedCommentContent,
                                               commentAuthorId, parentCommentId, deleterId, deleterName);
            } else {
                // This was a top-level comment - handle comment deletion notifications
                handleCommentDeletionNotifications(contentId, content, deletedCommentContent,
                                                 commentAuthorId, deleterId, deleterName);
            }
        } catch (Exception e) {
            // Log error but don't fail the main operation
            log.error("Failed to send comment deletion notification for content {}", contentId, e);
        }
    }

    /**
     * Handle notifications for reply deletion
     */
    private void handleReplyDeletionNotifications(String contentId, ContentGeneralInfoEntity content,
                                                String deletedReplyContent, Long replyAuthorId,
                                                Long parentCommentId, Long deleterId, String deleterName) {
        try {
            CommentEntity parentComment = commentRepository.findById(parentCommentId).orElse(null);
            if (parentComment == null) {
                log.warn("Parent comment {} not found for reply deletion notification", parentCommentId);
                return;
            }

            if (Boolean.TRUE.equals(parentComment.getDeleted())) {
                log.info("Parent comment {} is deleted - skipping reply deletion notification", parentCommentId);
                return;
            }

            // Notify the original comment author (if they are not the deleter)
            if (!deleterId.equals(parentComment.getUserId())) {
                log.info("Sending reply deletion notification to original comment author {} for reply by {} deleted by {}",
                        parentComment.getUserId(), replyAuthorId, deleterId);
                notificationService.notifyReplyDeleted(
                    contentId,
                    content.getTitle(),
                    content.getContentType(),
                    parentComment.getUserId(), // original comment author
                    deleterId,
                    deleterName,
                    deletedReplyContent,
                    parentComment.getContent() // original comment content for context
                );
            }

            // Also notify the reply author if they didn't delete it themselves
            if (!deleterId.equals(replyAuthorId) && !replyAuthorId.equals(parentComment.getUserId())) {
                log.info("Sending comment deletion notification to reply author {} for reply deleted by {}",
                        replyAuthorId, deleterId);
                notificationService.notifyCommentDeleted(
                    contentId,
                    content.getTitle(),
                    content.getContentType(),
                    replyAuthorId, // reply author
                    deleterId,
                    deleterName,
                    deletedReplyContent
                );
            }
        } catch (Exception e) {
            log.error("Failed to send reply deletion notifications for content {}", contentId, e);
        }
    }

    /**
     * Handle notifications for top-level comment deletion
     */
    private void handleCommentDeletionNotifications(String contentId, ContentGeneralInfoEntity content,
                                                  String deletedCommentContent, Long commentAuthorId,
                                                  Long deleterId, String deleterName) {
        try {
            // Notify the content owner if they didn't delete the comment and they are not the comment author
            if (!deleterId.equals(content.getOwnerId()) && !commentAuthorId.equals(content.getOwnerId())) {
                log.info("Sending comment deletion notification to content owner {} for comment by {} deleted by {}",
                        content.getOwnerId(), commentAuthorId, deleterId);
                notificationService.notifyCommentDeleted(
                    contentId,
                    content.getTitle(),
                    content.getContentType(),
                    content.getOwnerId(), // content owner
                    deleterId,
                    deleterName,
                    deletedCommentContent
                );
            }

            // Notify the comment author if they didn't delete it themselves
            if (!deleterId.equals(commentAuthorId)) {
                log.info("Sending comment deletion notification to comment author {} for comment deleted by {}",
                        commentAuthorId, deleterId);
                notificationService.notifyCommentDeleted(
                    contentId,
                    content.getTitle(),
                    content.getContentType(),
                    commentAuthorId, // comment author
                    deleterId,
                    deleterName,
                    deletedCommentContent
                );
            }
        } catch (Exception e) {
            log.error("Failed to send comment deletion notifications for content {}", contentId, e);
        }
    }

    /**
     * Like or unlike a comment
     * @param commentId comment ID
     * @param isLike true to like, false to unlike
     * @param currentUser authenticated user
     * @return updated like count
     */
    @Transactional
    public Long toggleCommentLike(Long commentId, boolean isLike, EnhancedUserDetail currentUser) {
        return commentRepository.findById(commentId)
            .map(comment -> {
                long currentLikeCount = comment.getLikeCount() == null ? 0L : comment.getLikeCount();
                long updatedCount = isLike ? currentLikeCount + 1 : Math.max(0L, currentLikeCount - 1);
                comment.setLikeCount(updatedCount);
                commentRepository.save(comment);
                return updatedCount;
            })
            .orElseGet(() -> {
                log.warn("Comment {} not found when toggling like", commentId);
                return 0L;
            });
    }

    /**
     * Check if current user has admin privileges
     * @param user current user
     * @return true if user has admin privileges
     */
    private boolean hasAdminPrivileges(EnhancedUserDetail user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().contains("ADMIN"));
    }

    /**
     * Convert CommentEntity to CommentDTO with proper time field handling
     * @param entity comment entity
     * @return comment DTO
     */
    private CommentDTO convertToDTO(CommentEntity entity) {
        // Use BasicConverter for direct conversion, Jackson will handle Timestamp serialization
        return BasicConverter.convert(entity, CommentDTO.class);
    }
}
