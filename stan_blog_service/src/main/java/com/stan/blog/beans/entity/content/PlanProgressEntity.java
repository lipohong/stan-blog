package com.stan.blog.beans.entity.content;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import com.stan.blog.beans.entity.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "stan_blog_plan_progress")
@SQLDelete(sql = "UPDATE stan_blog_plan_progress SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class PlanProgressEntity extends BaseEntity {

    @Id
    private String id;
    private String planId;
    private String description;
    private Long updaterId;

    @PrePersist
    public void prePersist() {
        super.prePersist();
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString().replace("-", "");
        }
    }
}