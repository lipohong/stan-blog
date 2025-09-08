package com.stan.blog.beans.entity.content;

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
@TableName("stan_blog_word_info")
public class WordEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String vocabularyId;
    private String text;
    private String meaningInChinese;
    private String meaningInEnglish;
    private String partOfSpeech;
}
