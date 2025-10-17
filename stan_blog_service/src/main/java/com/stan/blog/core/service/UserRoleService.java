package com.stan.blog.core.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.entity.user.UserRoleEntity;
import com.stan.blog.beans.repository.user.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final UserRoleRepository userRoleRepository;

    public UserRoleEntity save(UserRoleEntity entity) {
        return userRoleRepository.save(entity);
    }

    public List<UserRoleEntity> saveAll(List<UserRoleEntity> entities) {
        return userRoleRepository.saveAll(entities);
    }

    public List<UserRoleEntity> findByUserId(Long userId) {
        return userRoleRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteByUserId(Long userId) {
        userRoleRepository.deleteByUserId(userId);
    }
}