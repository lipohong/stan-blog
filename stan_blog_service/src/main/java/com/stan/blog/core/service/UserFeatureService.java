package com.stan.blog.core.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stan.blog.beans.entity.user.UserFeatureEntity;
import com.stan.blog.core.mapper.UserFeatureMapper;

@Service
public class UserFeatureService extends ServiceImpl<UserFeatureMapper, UserFeatureEntity> {
}
