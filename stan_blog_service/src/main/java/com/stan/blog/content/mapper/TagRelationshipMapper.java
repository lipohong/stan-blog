package com.stan.blog.content.mapper;

import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stan.blog.beans.dto.content.TagRelationshipDTO;
import com.stan.blog.beans.entity.tag.TagRelationshipEntity;

public interface TagRelationshipMapper extends BaseMapper<TagRelationshipEntity> {

    List<TagRelationshipDTO> getTagRelationshipByParentId(Long parentId, String collectionId);
}
