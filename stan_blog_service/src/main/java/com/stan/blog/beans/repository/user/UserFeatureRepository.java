package com.stan.blog.beans.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.user.UserFeatureEntity;

@Repository
public interface UserFeatureRepository extends JpaRepository<UserFeatureEntity, Long> {
}