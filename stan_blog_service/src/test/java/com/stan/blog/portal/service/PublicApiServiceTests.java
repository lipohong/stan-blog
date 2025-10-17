package com.stan.blog.portal.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import com.stan.blog.beans.consts.Const;
import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.beans.dto.content.ContentBasicInfoDTO;
import com.stan.blog.beans.dto.tag.TagInfoStatisticDTO;
import com.stan.blog.core.utils.CacheUtil;
// removed deprecated PublicApiMapper import
import com.stan.blog.beans.repository.content.ContentAdminRepository;
import com.stan.blog.beans.repository.content.ContentGeneralInfoRepository;
import com.stan.blog.beans.repository.user.UserRepository;
import com.stan.blog.content.service.impl.ContentTagService;

@ExtendWith(MockitoExtension.class)
class PublicApiServiceTests {


    @Mock
    private ContentGeneralInfoRepository contentGeneralInfoRepository;

    @Mock
    private ContentAdminRepository contentAdminRepository;

    @Mock
    private ContentTagService contentTagService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheUtil cacheUtil;

    @InjectMocks
    private PublicApiService publicApiService;

    private static final String CONTENT_ID = "content-123";

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient().when(cacheUtil.hIncr(any(), any(), anyLong())).thenReturn(1L);
    }

    @Test
    void searchContentsDelegatesToMapper() {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 20);
        when(contentGeneralInfoRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.jpa.domain.Specification.class),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(org.springframework.data.domain.Page.empty(pageable));

        Page<BaseContentDTO> result = publicApiService.searchContents(1, 20, new String[] {"T1"}, 7L, new String[] {"ARTICLE"}, Const.Topic.TECHNICAL, "blog");

        assertEquals(0, result.getTotalElements());
        verify(contentGeneralInfoRepository).findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.jpa.domain.Specification.class),
                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    void likeContentReturnsFalseWhenDuplicateLikeDetected() {
        when(cacheUtil.hasKey("LIKE_COUNT_UNI_KEY:127.0.0.1:" + CONTENT_ID)).thenReturn(true);

        Boolean liked = publicApiService.likeContent("127.0.0.1", CONTENT_ID);

        assertEquals(Boolean.FALSE, liked);
        verify(cacheUtil, never()).hIncr(any(), any(), anyLong());
        verify(cacheUtil, never()).set(any(), any(), anyLong());
    }

    @Test
    void likeContentIncrementsCacheWhenFirstLike() {
        when(cacheUtil.hasKey("LIKE_COUNT_UNI_KEY:10.0.0.2:" + CONTENT_ID)).thenReturn(false);

        Boolean liked = publicApiService.likeContent("10.0.0.2", CONTENT_ID);

        assertEquals(Boolean.TRUE, liked);
        verify(cacheUtil).hIncr("CONTENT_LIKE_COUNT", CONTENT_ID, 1L);
        verify(cacheUtil).set("LIKE_COUNT_UNI_KEY:10.0.0.2:" + CONTENT_ID, null, 300L);
    }

    @Test
    void getTagInfoStatisticsDelegatesToMapper() {
        // mock two contents with IDs
        com.stan.blog.beans.entity.content.ContentGeneralInfoEntity e1 = new com.stan.blog.beans.entity.content.ContentGeneralInfoEntity();
        e1.setId("c1");
        com.stan.blog.beans.entity.content.ContentGeneralInfoEntity e2 = new com.stan.blog.beans.entity.content.ContentGeneralInfoEntity();
        e2.setId("c2");
        when(contentGeneralInfoRepository.findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.jpa.domain.Specification.class)))
                .thenReturn(java.util.List.of(e1, e2));

        // both contents have same tag value 5
        com.stan.blog.beans.dto.tag.TagInfoDTO tag = new com.stan.blog.beans.dto.tag.TagInfoDTO();
        tag.setValue(5L);
        tag.setLabel("Tech");
        java.util.Map<String, java.util.List<com.stan.blog.beans.dto.tag.TagInfoDTO>> tagsMap = new java.util.HashMap<>();
        tagsMap.put("c1", java.util.List.of(tag));
        tagsMap.put("c2", java.util.List.of(tag));
        when(contentTagService.findTagsForContents(org.mockito.ArgumentMatchers.any()))
                .thenReturn(tagsMap);

        java.util.List<TagInfoStatisticDTO> result = publicApiService.getTagInfoStatistics(5L, new String[]{"PLAN"}, Const.Topic.LIFE, "goal");

        assertEquals(1, result.size());
        assertEquals(5L, result.get(0).getValue());
        assertEquals(2, result.get(0).getCount());
        verify(contentGeneralInfoRepository).findAll(org.mockito.ArgumentMatchers.any(org.springframework.data.jpa.domain.Specification.class));
        verify(contentTagService).findTagsForContents(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getContentBasicInfoDelegatesToMapper() {
        com.stan.blog.beans.entity.content.ContentGeneralInfoEntity general = new com.stan.blog.beans.entity.content.ContentGeneralInfoEntity();
        general.setId(CONTENT_ID);
        general.setContentType("ARTICLE");
        general.setPublicToAll(true);
        when(contentGeneralInfoRepository.findById(CONTENT_ID)).thenReturn(java.util.Optional.of(general));

        com.stan.blog.beans.entity.content.ContentAdminEntity admin = new com.stan.blog.beans.entity.content.ContentAdminEntity();
        admin.setContentId(CONTENT_ID);
        admin.setBanned(false);
        when(contentAdminRepository.findById(CONTENT_ID)).thenReturn(java.util.Optional.of(admin));

        ContentBasicInfoDTO result = publicApiService.getContentBasicInfo(CONTENT_ID);

        assertEquals(CONTENT_ID, result.getId());
        assertEquals("ARTICLE", result.getContentType());
        assertEquals(true, result.getPublicToAll());
        assertEquals(false, result.getBanned());
        verify(contentGeneralInfoRepository).findById(CONTENT_ID);
        verify(contentAdminRepository).findById(CONTENT_ID);
    }
}
