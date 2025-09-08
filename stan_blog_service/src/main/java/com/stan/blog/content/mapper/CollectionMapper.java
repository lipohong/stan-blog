package com.stan.blog.content.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stan.blog.beans.dto.content.BaseSearchFilter;
import com.stan.blog.beans.dto.content.CollectionDTO;
import com.stan.blog.beans.entity.content.CollectionEntity;

public interface CollectionMapper extends BaseContentMapper<CollectionDTO, CollectionEntity> {
    @Override
    Page<CollectionDTO> pageDTOsByUserId(Page<CollectionDTO> page, @Param("tableName") String tableName, @Param("userId") Long userId, BaseSearchFilter filter);

    @Override
    CollectionDTO getDTOById(@Param("tableName") String tableName, @Param("id") String id);
}
