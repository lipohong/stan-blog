package com.stan.blog.beans.entity.tag;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stan.blog.beans.dto.tag.TagInfoDTO;
import com.stan.blog.beans.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("stan_blog_tag_info")
public class TagInfoEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;

    public static TagInfoDTO covertToDTO(TagInfoEntity entity) {
        return TagInfoDTO.builder().value(entity.id).label(entity.keyword).build();
    }
}
