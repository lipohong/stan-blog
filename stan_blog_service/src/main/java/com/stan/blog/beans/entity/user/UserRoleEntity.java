package com.stan.blog.beans.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
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
@TableName("stan_blog_core_user_role")
public class UserRoleEntity extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String role;
    private Long userId;


    public UserRoleEntity(String role, Long userId) {
        this.role = role;
        this.userId = userId;
        this.deleted = Boolean.FALSE;
    }

    // to overwrite the super createBy that annotated by auto-filled
    private String createBy = "New Created";
}
