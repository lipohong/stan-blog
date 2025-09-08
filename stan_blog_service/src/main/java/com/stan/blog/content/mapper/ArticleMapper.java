package com.stan.blog.content.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stan.blog.beans.dto.content.ArticleDTO;
import com.stan.blog.beans.dto.content.BaseSearchFilter;
import com.stan.blog.beans.entity.content.ArticleEntity;

public interface ArticleMapper extends BaseContentMapper<ArticleDTO, ArticleEntity> {

    @Override
    Page<ArticleDTO> pageDTOsByUserId(Page<ArticleDTO> page, @Param("tableName") String tableName, @Param("userId") Long userId, BaseSearchFilter filter);

    @Override
    ArticleDTO getDTOById(@Param("tableName") String tableName, @Param("id") String id);
}
