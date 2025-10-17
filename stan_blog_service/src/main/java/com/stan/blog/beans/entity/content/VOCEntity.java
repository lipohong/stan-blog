package com.stan.blog.beans.entity.content;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Entity;
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
@Table(name = "stan_blog_vocabulary_info")
@SQLDelete(sql = "UPDATE stan_blog_vocabulary_info SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class VOCEntity extends BaseContentEntity {
    private String language;
}
