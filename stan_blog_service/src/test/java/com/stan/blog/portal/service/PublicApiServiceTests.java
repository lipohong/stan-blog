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

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stan.blog.beans.consts.Const;
import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.beans.dto.content.ContentBasicInfoDTO;
import com.stan.blog.beans.dto.tag.TagInfoStatisticDTO;
import com.stan.blog.core.utils.CacheUtil;
import com.stan.blog.portal.mapper.PublicApiMapper;

@ExtendWith(MockitoExtension.class)
class PublicApiServiceTests {

    @Mock
    private PublicApiMapper apiMapper;

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
        Page<BaseContentDTO> expectedPage = new Page<>();
        when(apiMapper.searchContents(any(), any(), any(), any(), any(), any())).thenReturn(expectedPage);

        Page<BaseContentDTO> result = publicApiService.searchContents(1, 20, new String[] {"T1"}, 7L, new String[] {"ARTICLE"}, Const.Topic.TECHNICAL, "blog");

        assertSame(expectedPage, result);
        verify(apiMapper).searchContents(any(), any(), any(), any(), any(), any());
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
        List<TagInfoStatisticDTO> expected = List.of(new TagInfoStatisticDTO());
        when(apiMapper.getTagInfoStatistics(eq(5L), org.mockito.ArgumentMatchers.<String[]>any(), eq(Const.Topic.LIFE), eq("goal")))
                .thenReturn(expected);

        List<TagInfoStatisticDTO> result = publicApiService.getTagInfoStatistics(5L, new String[]{"PLAN"}, Const.Topic.LIFE, "goal");

        assertSame(expected, result);
        ArgumentCaptor<String[]> captor = ArgumentCaptor.forClass(String[].class);
        verify(apiMapper).getTagInfoStatistics(eq(5L), captor.capture(), eq(Const.Topic.LIFE), eq("goal"));
        assertArrayEquals(new String[]{"PLAN"}, captor.getValue());
    }

    @Test
    void getContentBasicInfoDelegatesToMapper() {
        ContentBasicInfoDTO info = new ContentBasicInfoDTO();
        when(apiMapper.getContentBasicInfo(CONTENT_ID)).thenReturn(info);

        ContentBasicInfoDTO result = publicApiService.getContentBasicInfo(CONTENT_ID);

        assertSame(info, result);
        verify(apiMapper).getContentBasicInfo(CONTENT_ID);
    }
}
