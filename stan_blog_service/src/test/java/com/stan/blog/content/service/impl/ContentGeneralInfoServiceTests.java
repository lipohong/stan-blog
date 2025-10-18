package com.stan.blog.content.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import com.stan.blog.DefaultTestData;
import com.stan.blog.beans.consts.Const;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.beans.repository.content.ContentAdminRepository;
import com.stan.blog.beans.repository.content.ContentGeneralInfoRepository;
import com.stan.blog.beans.repository.user.UserRepository;

@ExtendWith(MockitoExtension.class)
class ContentGeneralInfoServiceTests {

    @Mock
    private ContentGeneralInfoRepository contentGeneralInfoRepository;

    @Mock
    private ContentAdminRepository contentAdminRepository;

    @Mock
    private ContentTagService contentTagService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContentGeneralInfoService contentGeneralInfoService;

    @BeforeEach
    void setUp() {
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

        org.mockito.Mockito.when(contentGeneralInfoRepository.findById("content-1"))
                .thenReturn(java.util.Optional.of(entity));

        contentGeneralInfoService.sinkViewCountToDB(Const.CONTENT_VIEW_COUNT_KEY, "content-1", 1L);

        assertEquals(6L, entity.getViewCount());
        verify(contentGeneralInfoRepository).save(entity);
    }

    @Test
    void sinkViewCountUpdatesLikeColumn() {
        ContentGeneralInfoEntity entity = new ContentGeneralInfoEntity();
        entity.setId("content-2");
        entity.setLikeCount(10L);

        org.mockito.Mockito.when(contentGeneralInfoRepository.findById("content-2"))
                .thenReturn(java.util.Optional.of(entity));

        contentGeneralInfoService.sinkViewCountToDB(Const.CONTENT_LIKE_COUNT_KEY, "content-2", 3L);

        assertEquals(13L, entity.getLikeCount());
        verify(contentGeneralInfoRepository).save(entity);
    }

    @Test
    void sinkViewCountIgnoresUnknownKey() {
        ContentGeneralInfoEntity entity = new ContentGeneralInfoEntity();
        entity.setId("content-3");

        org.mockito.Mockito.when(contentGeneralInfoRepository.findById("content-3"))
                .thenReturn(java.util.Optional.of(entity));

        contentGeneralInfoService.sinkViewCountToDB("UNKNOWN", "content-3", 5L);

        verify(contentGeneralInfoRepository, never()).save(any(ContentGeneralInfoEntity.class));
    }

    @Test
    void getAndValidateContentReturnsEntityWhenOwnerMatches() {
        ContentGeneralInfoEntity entity = new ContentGeneralInfoEntity();
        entity.setId("content-4");
        entity.setOwnerId(DefaultTestData.DefaultUser.USER_ID);

        org.mockito.Mockito.when(contentGeneralInfoRepository.findById("content-4"))
                .thenReturn(java.util.Optional.of(entity));

        ContentGeneralInfoEntity result = contentGeneralInfoService.getAndValidateContent("content-4");

        assertNotNull(result);
        assertEquals("content-4", result.getId());
    }

    @Test
    void getAndValidateContentThrowsWhenMissing() {
        org.mockito.Mockito.when(contentGeneralInfoRepository.findById("missing"))
                .thenReturn(java.util.Optional.empty());

        assertThrows(com.stan.blog.core.exception.StanBlogRuntimeException.class,
                () -> contentGeneralInfoService.getAndValidateContent("missing"));
    }

    @Test
    void getAndValidateContentThrowsWhenOwnerMismatch() {
        ContentGeneralInfoEntity entity = new ContentGeneralInfoEntity();
        entity.setId("content-5");
        entity.setOwnerId(999L);

        org.mockito.Mockito.when(contentGeneralInfoRepository.findById("content-5"))
                .thenReturn(java.util.Optional.of(entity));

        assertThrows(com.stan.blog.core.exception.StanBlogRuntimeException.class,
                () -> contentGeneralInfoService.getAndValidateContent("content-5"));
    }
}
