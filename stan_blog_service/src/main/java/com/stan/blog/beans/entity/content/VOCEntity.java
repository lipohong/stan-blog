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
@TableName("stan_blog_vocabulary_info")
public class VOCEntity extends BaseContentEntity {
    private String language;
}
