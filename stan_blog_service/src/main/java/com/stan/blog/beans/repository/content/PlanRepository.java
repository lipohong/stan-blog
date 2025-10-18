package com.stan.blog.beans.repository.content;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.content.PlanEntity;

@Repository
public interface PlanRepository extends JpaRepository<PlanEntity, String> {
}