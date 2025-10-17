package com.stan.blog.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

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

        UserRoleEntity saved = userRoleService.save(entity);
        assertNotNull(saved.getId());

        java.util.List<UserRoleEntity> stored = userRoleService.findByUserId(entity.getUserId());

        assertTrue(stored.stream().anyMatch(r -> "ROLE_TEST".equals(r.getRole())));
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

        UserFeatureEntity saved = userFeatureService.saveOrUpdate(feature);
        assertNotNull(saved.getUserId());

        UserFeatureEntity stored = userFeatureService.findByUserId(userId).orElse(null);

        assertNotNull(stored);
        assertTrue(stored.getArticleModule());
        assertEquals(Boolean.FALSE, stored.getPlanModule());
    }
}

