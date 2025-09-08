package com.stan.blog.beans.entity.user;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.stan.blog.beans.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("stan_blog_core_user_feature")
public class UserFeatureEntity extends BaseEntity {
    @TableId
    private Long userId;
    private Boolean articleModule;
    private Boolean planModule;
    private Boolean vocabularyModule;
    private Boolean collectionModule;
}
