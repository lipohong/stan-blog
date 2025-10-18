package com.stan.blog.content.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.stan.blog.beans.dto.content.BaseContentDTO;
import com.stan.blog.content.controller.impl.ContentAdminController;
import com.stan.blog.content.service.impl.ContentAdminService;

class ContentAdminControllerTests {

    private MockMvc mockMvc;
    private ContentAdminService contentAdminService;

    @BeforeEach
    void setUp() {
        contentAdminService = org.mockito.Mockito.mock(ContentAdminService.class);
        ContentAdminController controller = new ContentAdminController(contentAdminService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void banContentDelegatesToService() throws Exception {
        BaseContentDTO dto = new BaseContentDTO();
        dto.setId("content-1");
        when(contentAdminService.banContentById("content-1")).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/content-admin/{id}/ban", "content-1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("content-1"));

        verify(contentAdminService).banContentById("content-1");
    }

    @Test
    void unbanContentDelegatesToService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/v1/content-admin/{id}/unban", "content-2"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(contentAdminService).unbanContentById("content-2");
    }

    @Test
    void recommendContentDelegatesToService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/v1/content-admin/{id}/recommend", "content-3"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(contentAdminService).recommmendContentById("content-3");
    }

    @Test
    void unrecommendContentDelegatesToService() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/v1/content-admin/{id}/unrecommend", "content-4"))
                .andExpect(MockMvcResultMatchers.status().isOk());
        verify(contentAdminService).unrecommendContentById("content-4");
    }
}
