package com.stan.blog.beans.entity.content;

import java.sql.Timestamp;
import java.util.UUID;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.stan.blog.beans.consts.Const.Topic;
import com.stan.blog.beans.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
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
@Table(name = "stan_blog_content_general_info")
@SQLDelete(sql = "UPDATE stan_blog_content_general_info SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class ContentGeneralInfoEntity extends BaseEntity {
    @Id
    protected String id;
    protected String title;
    protected String description;
    protected String coverImgUrl;
    // manipulate if the content can be viewed by others
    protected Boolean publicToAll;
    protected Timestamp publishTime;
    protected Long viewCount;
    protected Long likeCount;
    protected Long ownerId;
    protected String contentType;
    // manipulate if the content require login to view
    protected Boolean contentProtected;

    @Column(name = "topic")
    @Enumerated(EnumType.STRING)
    protected Topic topic;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString().replace("-", "");
        }
    }
}
