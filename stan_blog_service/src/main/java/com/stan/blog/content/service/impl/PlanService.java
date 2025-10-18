package com.stan.blog.content.service.impl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.stan.blog.beans.consts.Const.ContentType;
import com.stan.blog.beans.dto.content.PlanCreationDTO;
import com.stan.blog.beans.dto.content.PlanDTO;
import com.stan.blog.beans.dto.content.PlanUpdateDTO;
import com.stan.blog.beans.entity.content.PlanEntity;
import com.stan.blog.beans.repository.content.PlanRepository;
import com.stan.blog.beans.repository.user.UserRepository;
import com.stan.blog.core.utils.CacheUtil;

@Service
public class PlanService
        extends BaseContentService<PlanDTO, PlanCreationDTO, PlanUpdateDTO, PlanEntity> {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository,
                       ContentGeneralInfoService contentGeneralInfoService,
                       ContentAdminService contentAdminService,
                       ContentTagService contentTagService,
                       UserRepository userRepository,
                       CacheUtil cacheUtil) {
        super(contentGeneralInfoService, contentAdminService, contentTagService, userRepository, cacheUtil);
        this.planRepository = planRepository;
    }

    @Override
    protected JpaRepository<PlanEntity, String> getRepository() {
        return planRepository;
    }

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