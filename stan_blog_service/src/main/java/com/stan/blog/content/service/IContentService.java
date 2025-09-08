package com.stan.blog.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stan.blog.beans.dto.content.BaseContentCreationDTO;
import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.beans.dto.content.BaseContentUpdateDTO;
import com.stan.blog.beans.dto.content.BaseSearchFilter;
import com.stan.blog.beans.dto.content.ContentVisibilityUpdateDTO;
import com.stan.blog.beans.entity.content.BaseContentEntity;
import com.stan.blog.content.mapper.BaseContentMapper;

public interface IContentService<
    D extends BaseContentDTO, 
    C extends BaseContentCreationDTO, 
    U extends BaseContentUpdateDTO, 
    E extends BaseContentEntity, 
    M extends BaseContentMapper<D, E>> {

    D save(C creationDTO);

    D update(U updateDTO);

    void delete(String id);

    Page<D> search(int current, int size, BaseSearchFilter filter);

    D getDTOById(String id);

    D getDTOByIdAndCount(String id);

    D updateVisibility(String id, ContentVisibilityUpdateDTO updateDTO);

}