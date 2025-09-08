package com.stan.blog.beans.entity.content;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("stan_blog_collection_info")
public class CollectionEntity extends BaseContentEntity {
}
