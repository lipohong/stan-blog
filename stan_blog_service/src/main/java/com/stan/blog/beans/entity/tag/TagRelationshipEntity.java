package com.stan.blog.beans.entity.tag;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stan.blog.beans.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("stan_blog_tag_relationship")
public class TagRelationshipEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long tagId;
    private Long parentId;
    private String collectionId;
}
