package com.stan.blog.content.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import com.stan.blog.beans.consts.Const;
import com.stan.blog.beans.dto.content.BaseContentCreationDTO;
import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.beans.dto.content.BaseContentUpdateDTO;
import com.stan.blog.beans.dto.content.ContentVisibilityUpdateDTO;
import com.stan.blog.beans.entity.content.BaseContentEntity;
import com.stan.blog.content.service.IContentService;

public interface IContentController<
    D extends BaseContentDTO,
    C extends BaseContentCreationDTO,
    U extends BaseContentUpdateDTO,
    E extends BaseContentEntity,
    S extends IContentService<D, C, U, E>> {

    ResponseEntity<D> create(C dto);

    ResponseEntity<D> update(String id, U dto);

    ResponseEntity<D> delete(String id);

    ResponseEntity<D> getById(String id);

    ResponseEntity<Page<D>> search(
        int current,
        int size,
        String status,
        String createFrom,
        String createTo,
        String updateFrom,
        String updateTo,
        String tags,
        String keyword,
        Const.Topic topic);

    ResponseEntity<D> updateVisibility(String id, ContentVisibilityUpdateDTO updateDTO);
}