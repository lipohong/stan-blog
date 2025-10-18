package com.stan.blog.beans.repository.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.content.PlanProgressEntity;

@Repository
public interface PlanProgressRepository extends JpaRepository<PlanProgressEntity, String> {

    Page<PlanProgressEntity> findByPlanIdOrderByCreateTimeDesc(String planId, Pageable pageable);
}