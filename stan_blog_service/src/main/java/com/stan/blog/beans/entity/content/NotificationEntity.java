package com.stan.blog.beans.entity.content;

import com.stan.blog.beans.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.stan.blog.beans.consts.Const.NotificationType;
import com.stan.blog.beans.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stan_blog_notification")
@SQLDelete(sql = "UPDATE stan_blog_notification SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class NotificationEntity extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * User ID who receives this notification
     */
    private Long recipientId;
    
    /**
     * User ID who triggered this notification (nullable for system notifications)
     */
    private Long senderId;
    
    /**
     * Type of notification
     */
    @Column(name = "notification_type")
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    
    /**
     * Notification title
     */
    private String title;
    
    /**
     * Notification message content
     */
    private String message;
    
    /**
     * Whether the notification has been read
     */
    private Boolean isRead = false;
    
    /**
     * Related content ID (for content-related notifications)
     */
    private String relatedContentId;
    
    /**
     * Related content type (ARTICLE, PLAN, VOCABULARY, COLLECTION)
     */
    private String relatedContentType;
    
    /**
     * Related content title for quick reference
     */
    private String relatedContentTitle;
    
    /**
     * URL to navigate when notification is clicked
     */
    private String actionUrl;
    
    /**
     * Additional metadata for the notification (JSON string)
     */
    private String metadata;
}