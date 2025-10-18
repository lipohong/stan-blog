package com.stan.blog.beans.entity.content;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "stan_blog_content_tag")
@jakarta.persistence.IdClass(ContentTagId.class)
public class ContentTagEntity {
    @jakarta.persistence.Id
    private String contentId;
    @jakarta.persistence.Id
    private Long tagId;
}
