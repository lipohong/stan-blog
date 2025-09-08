package com.stan.blog.core.service;


import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stan.blog.beans.entity.user.UserRoleEntity;
import com.stan.blog.core.mapper.UserRoleMapper;

@Service
public class UserRoleService extends ServiceImpl<UserRoleMapper, UserRoleEntity> {
}
