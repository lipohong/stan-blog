package com.stan.blog.content.service.impl;

import java.util.Objects;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stan.blog.beans.consts.Const.ContentType;
import com.stan.blog.beans.dto.content.PlanProgressCreationDTO;
import com.stan.blog.beans.dto.content.PlanProgressDTO;
import com.stan.blog.beans.dto.content.PlanProgressUpdateDTO;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.beans.entity.content.PlanProgressEntity;
import com.stan.blog.content.mapper.PlanProgressMapper;
import com.stan.blog.core.exception.StanBlogRuntimeException;
import com.stan.blog.core.utils.BasicConverter;
import com.stan.blog.core.utils.SecurityUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanProgressService extends ServiceImpl<PlanProgressMapper, PlanProgressEntity> {

    private final ContentGeneralInfoService contentGeneralInfoService;

    public Page<PlanProgressDTO> getProgressesByPlanId(String planId, int current, int size) {
        return this.baseMapper.getProgressDTOsByPlanId(new Page<>(current, size), planId);
    }

    public PlanProgressDTO getProgressesById(String id) {
        return this.baseMapper.getProgressDTOById(id);
    }

    @Transactional
    public PlanProgressDTO saveProgress(PlanProgressCreationDTO dto) {
        final ContentGeneralInfoEntity contentEntity = contentGeneralInfoService.getById(dto.getPlanId());
        if (Objects.isNull(contentEntity) || !ContentType.PLAN.name().equals(contentEntity.getContentType())) {
            throw new StanBlogRuntimeException("The planId is invalid");
        }
        final PlanProgressEntity entity = BasicConverter.convert(dto, PlanProgressEntity.class);
        entity.setUpdaterId(SecurityUtil.getUserId());
        this.save(entity);
        return this.baseMapper.getProgressDTOById(entity.getId());
    }

    @Transactional
    public PlanProgressDTO updateProgress(PlanProgressUpdateDTO dto) {
        final PlanProgressEntity progress = getAndValidateProgress(dto.getId());
        BeanUtils.copyProperties(dto, progress);
        progress.setUpdaterId(SecurityUtil.getUserId());
        this.baseMapper.updateById(progress);
        return this.getProgressesById(dto.getId());
    }

    private PlanProgressEntity getAndValidateProgress(String id) {
        final PlanProgressEntity progress = this.getById(id);
        validateProgressExists(progress);
        validateProgressOwner(progress);
        return progress;
    }

    private void validateProgressExists(PlanProgressEntity progress) {
        if (Objects.isNull(progress)) {
            throw new StanBlogRuntimeException("The progress doesn't exists, or already has been deleted");
        }
    }

    private void validateProgressOwner(PlanProgressEntity progress) {
        if (progress.getUpdaterId().longValue() != SecurityUtil.getUserId().longValue()) {
            throw new StanBlogRuntimeException("You can't operate a progress doesn't belong to you");
        }
    }
}
