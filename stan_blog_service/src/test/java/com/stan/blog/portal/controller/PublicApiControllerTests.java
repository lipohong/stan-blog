package com.stan.blog.portal.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stan.blog.analytics.service.AnalyticsService;
import com.stan.blog.beans.consts.Const;
import com.stan.blog.beans.dto.analytics.UserContentAnalyticsDTO;
import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.beans.dto.content.CollectionDTO;
import com.stan.blog.beans.dto.tag.TagInfoStatisticDTO;
import com.stan.blog.beans.dto.user.UserBriefProfileDTO;
import com.stan.blog.content.service.impl.ArticleService;
import com.stan.blog.content.service.impl.CollectionService;
import com.stan.blog.content.service.impl.PlanProgressService;
import com.stan.blog.content.service.impl.PlanService;
import com.stan.blog.content.service.impl.VOCService;
import com.stan.blog.content.service.impl.WordService;
import com.stan.blog.core.service.EmailVerificationService;
import com.stan.blog.core.service.PasswordResetService;
import com.stan.blog.core.service.UserService;
import com.stan.blog.core.utils.CacheUtil;
import com.stan.blog.core.utils.TagRelationshipUtil;
import com.stan.blog.portal.service.PublicApiService;

class PublicApiControllerTests {

    private MockMvc mockMvc;
    private PublicApiService publicApiService;
    private CollectionService collectionService;
    private PlanProgressService progressService;
    private WordService wordService;
    private UserService userService;
    private AnalyticsService analyticsService;
    private PasswordResetService passwordResetService;
    private EmailVerificationService emailVerificationService;
    private CacheUtil cacheUtil;
    private TagRelationshipUtil tagRelationshipUtil;

    @BeforeEach
    void setUp() {
        publicApiService = org.mockito.Mockito.mock(PublicApiService.class);
        ArticleService articleService = org.mockito.Mockito.mock(ArticleService.class);
        PlanService planService = org.mockito.Mockito.mock(PlanService.class);
        VOCService vocService = org.mockito.Mockito.mock(VOCService.class);
        collectionService = org.mockito.Mockito.mock(CollectionService.class);
        progressService = org.mockito.Mockito.mock(PlanProgressService.class);
        wordService = org.mockito.Mockito.mock(WordService.class);
        userService = org.mockito.Mockito.mock(UserService.class);
        analyticsService = org.mockito.Mockito.mock(AnalyticsService.class);
        passwordResetService = org.mockito.Mockito.mock(PasswordResetService.class);
        emailVerificationService = org.mockito.Mockito.mock(EmailVerificationService.class);
        cacheUtil = org.mockito.Mockito.mock(CacheUtil.class);
        tagRelationshipUtil = org.mockito.Mockito.mock(TagRelationshipUtil.class);
        when(analyticsService.getOverallResult(anyLong())).thenReturn(new UserContentAnalyticsDTO());

        PublicApiController controller = new PublicApiController(
            publicApiService,
            articleService,
            planService,
            vocService,
            collectionService,
            progressService,
            wordService,
            userService,
            analyticsService,
            passwordResetService,
            emailVerificationService,
            cacheUtil,
            tagRelationshipUtil
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void searchContentsDelegatesToService() throws Exception {
        Page<BaseContentDTO> page = new Page<>(1, 10);
        when(publicApiService.searchContents(1, 20, new String[] {}, null, new String[] {}, null, ""))
            .thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/api/contents")
                .param("size", "20"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(publicApiService).searchContents(1, 20, new String[] {}, null, new String[] {}, null, "");
    }

    @Test
    void likeContentReturnsBoolean() throws Exception {
        when(publicApiService.likeContent("127.0.0.1", "content-1")).thenReturn(Boolean.TRUE);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/api/like-content/{id}", "content-1")
                .with(request -> { request.setRemoteAddr("127.0.0.1"); return request; }))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));

        verify(publicApiService).likeContent("127.0.0.1", "content-1");
    }

    @Test
    void getBriefProfileFallsBackToServiceWhenCacheMiss() throws Exception {
        when(cacheUtil.get("USER_PROFILE_2")).thenReturn(null);
        UserBriefProfileDTO dto = new UserBriefProfileDTO();
        dto.setId(2L);
        when(userService.getUserBriefProfile(2L)).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/api/brief-profile/{id}", 2))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(2));

        verify(userService).getUserBriefProfile(2L);
        verify(cacheUtil).set(eq("USER_PROFILE_2"), any(), anyLong());
    }

    @Test
    void getCollectionByIdEnrichesTagTree() throws Exception {
        CollectionDTO dto = new CollectionDTO();
        dto.setId("col-1");
        dto.setOwnerId(5L);
        dto.setTagTree(List.of());
        when(collectionService.getDTOByIdAndCount("col-1")).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/api/collections/{id}", "col-1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("col-1"));

        verify(tagRelationshipUtil).setRelatedContentsForTagTree(List.of(), 5L);
    }

    @Test
    void requestPasswordResetDelegatesToService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/v1/api/request-password-reset")
                .param("email", "user@example.com"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(passwordResetService).requestPasswordReset("user@example.com");
    }

    @Test
    void getTagStatisticsDelegatesToService() throws Exception {
        when(publicApiService.getTagInfoStatistics(1L, new String[] {"ARTICLE"}, Const.Topic.OTHER, "key"))
                .thenReturn(List.of(new TagInfoStatisticDTO()));

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/api/tag-statistics")
                .param("ownerId", "1")
                .param("contentTypes", "ARTICLE")
                .param("topic", "OTHER")
                .param("keyword", "key"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(publicApiService).getTagInfoStatistics(1L, new String[] {"ARTICLE"}, Const.Topic.OTHER, "key");
    }
}
