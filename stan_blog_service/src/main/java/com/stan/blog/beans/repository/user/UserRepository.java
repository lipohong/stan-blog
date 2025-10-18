package com.stan.blog.beans.repository.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.user.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    Optional<UserEntity> findByUsernameOrEmailOrPhoneNum(String username, String email, String phoneNum);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}