package com.stan.blog.content.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.content.controller.impl.ContentGeneralInfoController;
import com.stan.blog.content.service.impl.ContentGeneralInfoService;

class ContentGeneralInfoControllerTests {

    private MockMvc mockMvc;
    private ContentGeneralInfoService contentService;

    @BeforeEach
    void setUp() {
        contentService = org.mockito.Mockito.mock(ContentGeneralInfoService.class);
        ContentGeneralInfoController controller = new ContentGeneralInfoController(contentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void searchPublishedContentsDelegatesToService() throws Exception {
        Page<BaseContentDTO> page = new Page<>(1, 10);
        when(contentService.searchPublishedContentDTOs(2, 20, "key", "recommended", "TECHNICAL")).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/contents")
                .param("current", "2")
                .param("size", "20")
                .param("keyword", "key")
                .param("status", "recommended")
                .param("topic", "TECHNICAL"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(contentService).searchPublishedContentDTOs(2, 20, "key", "recommended", "TECHNICAL");
    }
}
