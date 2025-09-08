package com.stan.blog.portal.mapper;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stan.blog.beans.consts.Const;
import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.beans.dto.content.ContentBasicInfoDTO;
import com.stan.blog.beans.dto.tag.TagInfoStatisticDTO;

public interface PublicApiMapper {
    Page<BaseContentDTO> searchContents(Page<BaseContentDTO> page, String[] tags, Long ownerId, String[] contentTypes, Const.Topic topic, String keyword);

    List<TagInfoStatisticDTO> getTagInfoStatistics(Long ownerId, String[] contentTypes, Const.Topic topic, String keyword);

    ContentBasicInfoDTO getContentBasicInfo(String contentId);

}
