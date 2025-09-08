package com.stan.blog.beans.entity.content;

import java.sql.Timestamp;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("stan_blog_plan_info")
public class PlanEntity extends BaseContentEntity {
    private Timestamp targetStartTime;
    private Timestamp targetEndTime;
}
