package com.stan.blog.content.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import com.stan.blog.DefaultTestData;
import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.beans.entity.content.ContentAdminEntity;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.beans.repository.content.ContentAdminRepository;
import com.stan.blog.core.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class ContentAdminServiceTests {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ContentGeneralInfoService contentGeneralInfoService;

    @Mock
    private ContentAdminRepository contentAdminRepository;

    @InjectMocks
    private ContentAdminService contentAdminService;

    @BeforeEach
    void setUp() {
        com.stan.blog.core.utils.SecurityUtil.setUserDetail(DefaultTestData.getDefaultUserAuthentication());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void banContentByIdMarksContentAsBannedAndNotifiesOwner() {
        ContentAdminEntity adminEntity = new ContentAdminEntity();
        adminEntity.setContentId("content-1");
        adminEntity.setBanned(false);

        ContentGeneralInfoEntity generalInfo = new ContentGeneralInfoEntity();
        generalInfo.setId("content-1");
        generalInfo.setTitle("First Article");
        generalInfo.setContentType("ARTICLE");
        generalInfo.setOwnerId(8L);

        when(contentAdminRepository.findById("content-1")).thenReturn(java.util.Optional.of(adminEntity));
        when(contentAdminRepository.save(any(ContentAdminEntity.class))).thenReturn(adminEntity);
        when(contentGeneralInfoService.findById("content-1")).thenReturn(generalInfo);

        BaseContentDTO result = contentAdminService.banContentById("content-1");

        verify(contentAdminRepository).save(adminEntity);
        verify(notificationService).notifyContentBanned("content-1", "First Article", "ARTICLE", 8L, 1L);
        assertTrue(adminEntity.getBanned());
        assertEquals("Violating content, blocked by administrators", adminEntity.getReason());
        assertNotNull(result);
        assertTrue(Boolean.TRUE.equals(result.getBanned()));
    }

    @Test
    void unrecommendContentClearsRecommendationFlag() {
        ContentAdminEntity adminEntity = new ContentAdminEntity();
        adminEntity.setContentId("content-2");
        adminEntity.setRecommended(true);

        when(contentAdminRepository.findById("content-2")).thenReturn(java.util.Optional.of(adminEntity));
        when(contentAdminRepository.save(any(ContentAdminEntity.class))).thenReturn(adminEntity);

        BaseContentDTO result = contentAdminService.unrecommendContentById("content-2");

        verify(contentAdminRepository).save(adminEntity);
        assertFalse(adminEntity.getRecommended());
        assertEquals("Content recommendation has been removed by administrators", adminEntity.getReason());
        assertNotNull(result);
        assertTrue(Boolean.FALSE.equals(result.getRecommended()));
    }

    @Test
    void recommendContentByIdMarksContentAndSendsNotification() {
        ContentAdminEntity adminEntity = new ContentAdminEntity();
        adminEntity.setContentId("content-3");
        adminEntity.setRecommended(false);

        ContentGeneralInfoEntity generalInfo = new ContentGeneralInfoEntity();
        generalInfo.setId("content-3");
        generalInfo.setTitle("Awesome Plan");
        generalInfo.setContentType("PLAN");
        generalInfo.setOwnerId(5L);

        when(contentAdminRepository.findById("content-3")).thenReturn(java.util.Optional.of(adminEntity));
        when(contentAdminRepository.save(any(ContentAdminEntity.class))).thenReturn(adminEntity);
        when(contentGeneralInfoService.findById("content-3")).thenReturn(generalInfo);

        BaseContentDTO result = contentAdminService.recommmendContentById("content-3");

        verify(notificationService).notifyContentRecommended("content-3", "Awesome Plan", "PLAN", 5L, 1L);
        assertTrue(adminEntity.getRecommended());
        assertEquals("High-quality content, recommended by administrators", adminEntity.getReason());
        assertNotNull(result);
        assertTrue(Boolean.TRUE.equals(result.getRecommended()));
    }
}
