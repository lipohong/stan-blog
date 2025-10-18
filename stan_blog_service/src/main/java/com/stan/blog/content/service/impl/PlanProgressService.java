package com.stan.blog.content.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.consts.Const.ContentType;
import com.stan.blog.beans.dto.content.PlanProgressCreationDTO;
import com.stan.blog.beans.dto.content.PlanProgressDTO;
import com.stan.blog.beans.dto.content.PlanProgressUpdateDTO;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.beans.entity.content.PlanProgressEntity;
import com.stan.blog.beans.entity.user.UserEntity;
import com.stan.blog.beans.repository.content.PlanProgressRepository;
import com.stan.blog.beans.repository.user.UserRepository;
import com.stan.blog.core.exception.StanBlogRuntimeException;
import com.stan.blog.core.utils.BasicConverter;
import com.stan.blog.core.utils.SecurityUtil;
import com.stan.blog.core.utils.UserDisplayNameUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlanProgressService {

    private final PlanProgressRepository planProgressRepository;
    private final ContentGeneralInfoService contentGeneralInfoService;
    private final UserRepository userRepository;

    public Page<PlanProgressDTO> getProgressesByPlanId(String planId, int current, int size) {
        Pageable pageable = PageRequest.of(Math.max(current - 1, 0), Math.max(size, 1));
        Page<PlanProgressEntity> page = planProgressRepository.findByPlanIdOrderByCreateTimeDesc(planId, pageable);
        if (page.isEmpty()) {
            return Page.empty(pageable);
        }
        Map<Long, UserEntity> userMap = loadUsers(page.getContent());
        List<PlanProgressDTO> content = page.getContent().stream()
                .map(entity -> toDto(entity, userMap.get(entity.getUpdaterId())))
                .toList();
        return new PageImpl<>(content, pageable, page.getTotalElements());
    }

    public PlanProgressDTO getProgressesById(String id) {
        return planProgressRepository.findById(id)
                .map(entity -> toDto(entity, loadUser(entity.getUpdaterId())))
                .orElse(null);
    }

    @org.springframework.transaction.annotation.Transactional
    public com.stan.blog.beans.dto.content.PlanProgressDTO saveProgress(
            com.stan.blog.beans.dto.content.PlanProgressCreationDTO creationDTO) {
        if (creationDTO.getDescription() != null && creationDTO.getDescription().length() > 1000) {
             throw new com.stan.blog.core.exception.StanBlogRuntimeException("Description can not exceed 1000 characters");
         }
        ContentGeneralInfoEntity contentEntity = contentGeneralInfoService.findById(creationDTO.getPlanId());
        if (Objects.isNull(contentEntity) || !ContentType.PLAN.name().equals(contentEntity.getContentType())) {
            throw new StanBlogRuntimeException("The planId is invalid");
        }
        PlanProgressEntity entity = new PlanProgressEntity();
        entity.setPlanId(creationDTO.getPlanId());
        entity.setDescription(creationDTO.getDescription());
        entity.setUpdaterId(SecurityUtil.getUserId());
        PlanProgressEntity saved = planProgressRepository.save(entity);
        return getProgressesById(saved.getId());
    }

    @org.springframework.transaction.annotation.Transactional
    public com.stan.blog.beans.dto.content.PlanProgressDTO updateProgress(
            com.stan.blog.beans.dto.content.PlanProgressUpdateDTO updateDTO) {
        if (updateDTO.getDescription() != null && updateDTO.getDescription().length() > 1000) {
             throw new com.stan.blog.core.exception.StanBlogRuntimeException("Description can not exceed 1000 characters");
         }
        PlanProgressEntity progress = getAndValidateProgress(updateDTO.getId());
        progress.setDescription(updateDTO.getDescription());
        progress.setUpdaterId(SecurityUtil.getUserId());
        PlanProgressEntity saved = planProgressRepository.save(progress);
        return getProgressesById(saved.getId());
    }

    @Transactional
    public void deleteProgressById(String id) {
        PlanProgressEntity progress = getAndValidateProgress(id);
        planProgressRepository.delete(progress);
    }

    private PlanProgressEntity getAndValidateProgress(String id) {
        PlanProgressEntity progress = planProgressRepository.findById(id)
                .orElseThrow(() -> new StanBlogRuntimeException("The progress doesn't exist, or already has been deleted"));
        validateProgressOwner(progress);
        return progress;
    }

    private void validateProgressOwner(PlanProgressEntity progress) {
        if (!Objects.equals(progress.getUpdaterId(), SecurityUtil.getUserId())) {
            throw new StanBlogRuntimeException("You can't operate a progress that doesn't belong to you");
        }
    }

    private PlanProgressDTO toDto(PlanProgressEntity entity, UserEntity user) {
        PlanProgressDTO dto = BasicConverter.convert(entity, PlanProgressDTO.class);
        if (user != null) {
            dto.setUpdaterName(UserDisplayNameUtil.buildDisplayName(user));
            dto.setAvatarUrl(user.getAvatarUrl());
        }
        return dto;
    }

    private Map<Long, UserEntity> loadUsers(List<PlanProgressEntity> entities) {
        Set<Long> userIds = entities.stream()
                .map(PlanProgressEntity::getUpdaterId)
                .collect(Collectors.toSet());
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, entity -> entity));
    }

    private UserEntity loadUser(Long userId) {
        return userId == null ? null : userRepository.findById(userId).orElse(null);
    }
}