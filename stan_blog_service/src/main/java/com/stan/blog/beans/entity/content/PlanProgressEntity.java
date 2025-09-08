package com.stan.blog.beans.entity.content;

import com.baomidou.mybatisplus.annotation.TableName;
import com.stan.blog.beans.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("stan_blog_plan_progress")
public class PlanProgressEntity extends BaseEntity {
    private String id;
    private String planId;
    private String description;
    private Long updaterId;
}
