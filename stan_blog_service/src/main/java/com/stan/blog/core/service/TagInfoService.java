package com.stan.blog.core.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stan.blog.beans.dto.tag.TagInfoCreationDTO;
import com.stan.blog.beans.dto.tag.TagInfoDTO;
import com.stan.blog.beans.entity.tag.TagInfoEntity;
import com.stan.blog.core.exception.StanBlogRuntimeException;
import com.stan.blog.core.mapper.TagInfoMapper;
import com.stan.blog.core.utils.BasicConverter;

@Service
public class TagInfoService extends ServiceImpl<TagInfoMapper, TagInfoEntity> {

    public Page<TagInfoDTO> getTagsByKeyword(String keyword, int current, int size) {
        return (Page<TagInfoDTO>) this.page(new Page<>(current, size), new LambdaQueryWrapper<TagInfoEntity>()
                .like(TagInfoEntity::getKeyword, keyword)
                .orderByAsc(TagInfoEntity::getKeyword))
                .convert(TagInfoEntity::covertToDTO);
    }

    public TagInfoDTO createTag(TagInfoCreationDTO dto) {
        validateTagExistOrNot(dto);
        final TagInfoEntity entity = BasicConverter.convert(dto, TagInfoEntity.class);
        this.save(entity);
        return TagInfoEntity.covertToDTO(entity);
    }

    private void validateTagExistOrNot(TagInfoCreationDTO dto) {
        if (this.baseMapper.exists(new LambdaQueryWrapper<TagInfoEntity>()
                .eq(TagInfoEntity::getKeyword, dto.getKeyword()))) {
            throw new StanBlogRuntimeException("Tag already exists in DB");
        }
    }
}
