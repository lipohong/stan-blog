package com.stan.blog.core.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.entity.user.UserFeatureEntity;
import com.stan.blog.beans.repository.user.UserFeatureRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserFeatureService {

    private final UserFeatureRepository userFeatureRepository;

    public UserFeatureEntity save(UserFeatureEntity entity) {
        return userFeatureRepository.save(entity);
    }

    @Transactional
    public UserFeatureEntity saveOrUpdate(UserFeatureEntity entity) {
        return userFeatureRepository.save(entity);
    }

    public Optional<UserFeatureEntity> findByUserId(Long userId) {
        return userFeatureRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        userFeatureRepository.deleteByUserId(userId);
    }
}