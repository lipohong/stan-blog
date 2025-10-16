package com.stan.blog.beans.repository.content;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.stan.blog.beans.entity.content.PlanProgressEntity;

@Repository
public interface PlanProgressRepository extends JpaRepository<PlanProgressEntity, String> {

    @Query(value = "SELECT * FROM stan_blog_plan_progress WHERE plan_id = :planId AND deleted != true ORDER BY create_time DESC", nativeQuery = true)
    Page<PlanProgressEntity> findByPlanIdOrderByCreateTimeDesc(String planId, Pageable pageable);
}