package com.stan.blog.beans.entity.content;

import com.stan.blog.beans.entity.BaseEntity;
import com.stan.blog.core.exception.StanBlogRuntimeException;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@MappedSuperclass
public abstract class BaseContentEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected String contentId;

    public String getTableName() {
        Table table = this.getClass().getAnnotation(Table.class);
        if (StringUtils.isBlank(table.name())) {
            throw new StanBlogRuntimeException("Entity must have a valid TableName value");
        }
        return table.name();
    }
}
