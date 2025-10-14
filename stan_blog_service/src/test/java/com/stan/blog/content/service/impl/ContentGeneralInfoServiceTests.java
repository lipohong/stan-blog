package com.stan.blog.content.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.stan.blog.DefaultTestData;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.content.mapper.ContentGeneralInfoMapper;
import com.stan.blog.core.exception.StanBlogRuntimeException;

@ExtendWith(MockitoExtension.class)
class ContentGeneralInfoServiceTests {

    @Mock
    private ContentGeneralInfoMapper contentGeneralInfoMapper;

    @Spy
    @InjectMocks
    private ContentGeneralInfoService contentGeneralInfoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(contentGeneralInfoService, "baseMapper", contentGeneralInfoMapper);
        com.stan.blog.core.utils.SecurityUtil.setUserDetail(DefaultTestData.getDefaultUserAuthentication());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void sinkViewCountUpdatesViewColumn() {
        ContentGeneralInfoEntity entity = new ContentGeneralInfoEntity();
        entity.setId("content-1");
        entity.setViewCount(5L);

        doReturn(entity).when(contentGeneralInfoService).getById("content-1");

        contentGeneralInfoService.sinkViewCountToDB("CONTENT_VIEW_COUNT", "content-1", 1L);

        verify(contentGeneralInfoMapper).updateContentMetaCount("VIEW_COUNT", "content-1", 6L);
    }

    @Test
    void sinkViewCountUpdatesLikeColumn() {
        ContentGeneralInfoEntity entity = new ContentGeneralInfoEntity();
        entity.setId("content-2");
        entity.setLikeCount(10L);

        doReturn(entity).when(contentGeneralInfoService).getById("content-2");

        contentGeneralInfoService.sinkViewCountToDB("CONTENT_LIKE_COUNT", "content-2", 3L);

        verify(contentGeneralInfoMapper).updateContentMetaCount("LIKE_COUNT", "content-2", 13L);
    }

    @Test
    void sinkViewCountIgnoresUnknownKey() {
        ContentGeneralInfoEntity entity = new ContentGeneralInfoEntity();
        entity.setId("content-3");

        doReturn(entity).when(contentGeneralInfoService).getById("content-3");

        contentGeneralInfoService.sinkViewCountToDB("UNKNOWN", "content-3", 5L);

        verify(contentGeneralInfoMapper, never()).updateContentMetaCount(any(), any(), anyLong());
    }

    @Test
    void getAndValidateContentReturnsEntityWhenOwnerMatches() {
        ContentGeneralInfoEntity entity = new ContentGeneralInfoEntity();
        entity.setId("content-4");
        entity.setOwnerId(DefaultTestData.DefaultUser.USER_ID);

        doReturn(entity).when(contentGeneralInfoService).getById("content-4");

        ContentGeneralInfoEntity result = contentGeneralInfoService.getAndValidateContent("content-4");

        assertNotNull(result);
        assertEquals("content-4", result.getId());
    }

    @Test
    void getAndValidateContentThrowsWhenMissing() {
        doReturn(null).when(contentGeneralInfoService).getById("missing");

        assertThrows(StanBlogRuntimeException.class, () -> contentGeneralInfoService.getAndValidateContent("missing"));
    }

    @Test
    void getAndValidateContentThrowsWhenOwnerMismatch() {
        ContentGeneralInfoEntity entity = new ContentGeneralInfoEntity();
        entity.setId("content-5");
        entity.setOwnerId(999L);

        doReturn(entity).when(contentGeneralInfoService).getById("content-5");

        assertThrows(StanBlogRuntimeException.class, () -> contentGeneralInfoService.getAndValidateContent("content-5"));
    }
}
