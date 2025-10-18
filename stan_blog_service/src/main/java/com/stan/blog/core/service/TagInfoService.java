package com.stan.blog.core.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.dto.tag.TagInfoCreationDTO;
import com.stan.blog.beans.dto.tag.TagInfoDTO;
import com.stan.blog.beans.entity.tag.TagInfoEntity;
import com.stan.blog.beans.repository.tag.TagInfoRepository;
import com.stan.blog.core.exception.StanBlogRuntimeException;
import com.stan.blog.core.utils.BasicConverter;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagInfoService {

    private final TagInfoRepository tagInfoRepository;

    public Page<TagInfoDTO> getTagsByKeyword(String keyword, int current, int size) {
        Pageable pageable = PageRequest.of(Math.max(current - 1, 0), Math.max(size, 1),
                Sort.by(Sort.Direction.ASC, "keyword"));
        Page<TagInfoEntity> page;
        if (keyword == null || keyword.isBlank()) {
            page = tagInfoRepository.findAll(pageable);
        } else {
            page = tagInfoRepository.findByKeywordContainingIgnoreCase(keyword.trim(), pageable);
        }
        return page.map(TagInfoEntity::covertToDTO);
    }

    @Transactional
    public TagInfoDTO createTag(TagInfoCreationDTO dto) {
        validateTagExistOrNot(dto);
        TagInfoEntity entity = BasicConverter.convert(dto, TagInfoEntity.class);
        TagInfoEntity saved = tagInfoRepository.save(entity);
        return TagInfoEntity.covertToDTO(saved);
    }

    private void validateTagExistOrNot(TagInfoCreationDTO dto) {
        if (tagInfoRepository.existsByKeywordIgnoreCase(dto.getKeyword())) {
            throw new StanBlogRuntimeException("Tag already exists in DB");
        }
    }
}