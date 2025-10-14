package com.stan.blog.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stan.blog.beans.entity.user.UserFeatureEntity;
import com.stan.blog.beans.entity.user.UserRoleEntity;

@SpringBootTest
@ActiveProfiles("test")
class UserRoleAndFeatureServiceIntegrationTests {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserFeatureService userFeatureService;

    @Test
    void saveUserRoleAndQueryByUserId() {
        UserRoleEntity entity = new UserRoleEntity();
        entity.setRole("ROLE_TEST");
        entity.setUserId(1000L + (long)(Math.random() * 1000));

        assertTrue(userRoleService.save(entity));

        UserRoleEntity stored = userRoleService.getOne(new LambdaQueryWrapper<UserRoleEntity>()
                .eq(UserRoleEntity::getUserId, entity.getUserId()));

        assertNotNull(stored);
        assertEquals("ROLE_TEST", stored.getRole());
    }

    @Test
    void saveUserFeatureAndRetrieveById() {
        Long userId = 2000L + (long)(Math.random() * 1000);
        UserFeatureEntity feature = new UserFeatureEntity();
        feature.setUserId(userId);
        feature.setArticleModule(Boolean.TRUE);
        feature.setPlanModule(Boolean.FALSE);
        feature.setVocabularyModule(Boolean.TRUE);
        feature.setCollectionModule(Boolean.TRUE);

        assertTrue(userFeatureService.saveOrUpdate(feature));

        UserFeatureEntity stored = userFeatureService.getById(userId);

        assertNotNull(stored);
        assertTrue(stored.getArticleModule());
        assertEquals(Boolean.FALSE, stored.getPlanModule());
    }
}

