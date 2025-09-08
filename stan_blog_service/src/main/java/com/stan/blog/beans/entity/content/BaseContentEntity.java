package com.stan.blog.beans.entity.content;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stan.blog.beans.entity.BaseEntity;
import com.stan.blog.core.exception.StanBlogRuntimeException;

import io.micrometer.common.util.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseContentEntity extends BaseEntity {
    @TableId(type = IdType.INPUT)
    protected String contentId;

    public String getTableName() {
        TableName name = this.getClass().getAnnotation(TableName.class);
        if (StringUtils.isBlank(name.value())) {
            throw new StanBlogRuntimeException("Entity must have a valid TableName value");
        }
        return name.value();
    }
}
