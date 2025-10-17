package com.stan.blog.beans.entity.content;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "stan_blog_collection_info")
public class CollectionEntity extends BaseContentEntity {
}
