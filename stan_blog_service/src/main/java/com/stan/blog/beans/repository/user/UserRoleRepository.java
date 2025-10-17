package com.stan.blog.beans.repository.user;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.user.UserRoleEntity;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, Long> {
    List<UserRoleEntity> findByUserId(Long userId);
    void deleteByUserId(Long userId);
}