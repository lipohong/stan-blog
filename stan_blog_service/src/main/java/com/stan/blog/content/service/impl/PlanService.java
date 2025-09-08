package com.stan.blog.content.service.impl;

import org.springframework.stereotype.Service;

import com.stan.blog.beans.consts.Const.ContentType;
import com.stan.blog.beans.dto.content.PlanCreationDTO;
import com.stan.blog.beans.dto.content.PlanDTO;
import com.stan.blog.beans.dto.content.PlanUpdateDTO;
import com.stan.blog.beans.entity.content.PlanEntity;
import com.stan.blog.content.mapper.PlanMapper;

@Service
public class PlanService
        extends BaseContentService<PlanDTO, PlanCreationDTO, PlanUpdateDTO, PlanEntity, PlanMapper> {

    @Override
    protected PlanDTO getConcreteDTO() {
        return new PlanDTO();
    }

    @Override
    protected PlanEntity getConcreteEntity() {
        return new PlanEntity();
    }

    @Override
    protected ContentType getContentType() {
        return ContentType.PLAN;
    }

}
