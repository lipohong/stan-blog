package com.stan.blog.beans.entity.content;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stan.blog.beans.entity.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("stan_blog_content_admin")
public class ContentAdminEntity extends BaseEntity {
    @TableId(type = IdType.INPUT)
    private String contentId;
    private Boolean recommended;
    private Boolean banned;
    private String reason;
}
