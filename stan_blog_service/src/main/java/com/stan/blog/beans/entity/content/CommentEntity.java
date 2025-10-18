package com.stan.blog.beans.entity.content;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.stan.blog.beans.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stan_blog_comment")
@SQLDelete(sql = "UPDATE stan_blog_comment SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class CommentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Content ID that this comment belongs to
     */
    private String contentId;

    /**
     * Content type (ARTICLE, PLAN, VOCABULARY, etc.)
     */
    private String contentType;

    /**
     * Comment content
     */
    private String content;

    /**
     * User ID who created this comment
     */
    private Long userId;

    /**
     * User name who created this comment
     */
    private String userName;

    /**
     * User avatar URL
     */
    private String userAvatarUrl;

    /**
     * Parent comment ID for nested comments (null for top-level comments)
     */
    private Long parentId;

    /**
     * Reply to user name (for quoted replies)
     */
    private String replyToUserName;

    /**
     * Reply to content snippet (for quoted replies, max 100 chars)
     */
    private String replyToContent;

    /**
     * Number of likes for this comment
     */
    private Long likeCount = 0L;

    /**
     * IP address of the commenter
     */
    private String ipAddress;
} 