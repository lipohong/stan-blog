package com.stan.blog.analytics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stan.blog.beans.dto.analytics.UserContentAnalyticsDTO;
import com.stan.blog.beans.entity.content.ContentGeneralInfoEntity;
import com.stan.blog.beans.repository.content.ContentGeneralInfoRepository;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTests {

    @Mock
    private ContentGeneralInfoRepository contentGeneralInfoRepository;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void getOverallResultAggregatesStatisticsAcrossAllContent() {
        ContentGeneralInfoEntity publicContent = new ContentGeneralInfoEntity();
        publicContent.setLikeCount(7L);
        publicContent.setViewCount(11L);
        publicContent.setPublicToAll(true);

        ContentGeneralInfoEntity privateContent = new ContentGeneralInfoEntity();
        privateContent.setLikeCount(3L);
        privateContent.setViewCount(5L);
        privateContent.setPublicToAll(false);

        when(contentGeneralInfoRepository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<ContentGeneralInfoEntity>>any()))
            .thenReturn(List.of(publicContent, privateContent));

        UserContentAnalyticsDTO result = analyticsService.getOverallResult(42L);

        assertEquals(2, result.getTotalCount());
        assertEquals(10L, result.getTotalLikeCount());
        assertEquals(16L, result.getTotalViewCount());
        assertEquals(1, result.getPublicCount());
        verify(contentGeneralInfoRepository).findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<ContentGeneralInfoEntity>>any());
    }

    @Test
    void getResultAggregatesOnlyFilteredContentType() {
        ContentGeneralInfoEntity vocabulary = new ContentGeneralInfoEntity();
        vocabulary.setLikeCount(2L);
        vocabulary.setViewCount(9L);
        vocabulary.setPublicToAll(true);

        when(contentGeneralInfoRepository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<ContentGeneralInfoEntity>>any()))
            .thenReturn(List.of(vocabulary));

        UserContentAnalyticsDTO result = analyticsService.getResult("VOC", 99L);

        assertEquals(1, result.getTotalCount());
        assertEquals(2L, result.getTotalLikeCount());
        assertEquals(9L, result.getTotalViewCount());
        assertEquals(1, result.getPublicCount());
        verify(contentGeneralInfoRepository).findAll(org.mockito.ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<ContentGeneralInfoEntity>>any());
    }
}
