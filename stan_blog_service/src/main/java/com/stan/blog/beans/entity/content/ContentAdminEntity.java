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
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "stan_blog_content_admin")
@SQLDelete(sql = "UPDATE stan_blog_content_admin SET deleted = true WHERE content_id = ?")
@SQLRestriction("deleted = false")
public class ContentAdminEntity extends BaseEntity {
    @Id 
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String contentId;
    private Boolean recommended;
    private Boolean banned;
    private String reason;
}
