package com.stan.blog.beans.entity.content;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("stan_blog_article_info")
public class ArticleEntity extends BaseContentEntity {
    private String subTitle;
    private String content;
}
