package com.stan.blog.analytics.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.stan.blog.DefaultTestData;
import com.stan.blog.analytics.service.AnalyticsService;
import com.stan.blog.beans.dto.analytics.UserContentAnalyticsDTO;
import com.stan.blog.core.utils.SecurityUtil;

class AnalyticsControllerTests {

    private MockMvc mockMvc;
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        analyticsService = mock(AnalyticsService.class);
        AnalyticsController controller = new AnalyticsController(analyticsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityUtil.setUserDetail(DefaultTestData.getDefaultUserAuthentication());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getUserContentAnalyticsReturnsAggregatedResult() throws Exception {
        UserContentAnalyticsDTO dto = new UserContentAnalyticsDTO();
        dto.setTotalCount(3);
        dto.setTotalLikeCount(15L);
        dto.setTotalViewCount(120L);
        dto.setPublicCount(2);

        when(analyticsService.getResult("ARTICLE", DefaultTestData.DefaultUser.USER_ID)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/analytics/{type}", "ARTICLE")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalCount").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalLikeCount").value(15))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalViewCount").value(120))
                .andExpect(MockMvcResultMatchers.jsonPath("$.publicCount").value(2));

        verify(analyticsService).getResult(eq("ARTICLE"), eq(DefaultTestData.DefaultUser.USER_ID));
    }
}
