package com.stan.blog.beans.entity.content;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("stan_blog_content_tag")
public class ContentTagEntity {
    private String contentId;
    private Long tagId;
}
