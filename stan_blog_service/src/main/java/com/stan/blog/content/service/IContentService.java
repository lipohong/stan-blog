package com.stan.blog.content.service;

import org.springframework.data.domain.Page;

import com.stan.blog.beans.dto.content.BaseContentCreationDTO;
import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.beans.dto.content.BaseContentUpdateDTO;
import com.stan.blog.beans.dto.content.BaseSearchFilter;
import com.stan.blog.beans.dto.content.ContentVisibilityUpdateDTO;
import com.stan.blog.beans.entity.content.BaseContentEntity;

public interface IContentService<
    D extends BaseContentDTO,
    C extends BaseContentCreationDTO,
    U extends BaseContentUpdateDTO,
    E extends BaseContentEntity> {

    D save(C creationDTO);

    D update(U updateDTO);

    void delete(String id);

    Page<D> search(int current, int size, BaseSearchFilter filter);

    D getDTOById(String id);

    D getDTOByIdAndCount(String id);

    D updateVisibility(String id, ContentVisibilityUpdateDTO updateDTO);
}