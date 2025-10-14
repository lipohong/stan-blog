package com.stan.blog.content.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stan.blog.beans.dto.content.ArticleCreationDTO;
import com.stan.blog.beans.dto.content.ArticleDTO;
import com.stan.blog.beans.dto.content.ArticleUpdateDTO;
import com.stan.blog.beans.dto.content.ContentVisibilityUpdateDTO;
import com.stan.blog.content.controller.impl.ArticleController;
import com.stan.blog.content.service.impl.ArticleService;

class ArticleControllerTests {

    private MockMvc mockMvc;
    private ArticleService articleService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        articleService = org.mockito.Mockito.mock(ArticleService.class);
        ArticleController controller = new ArticleController(articleService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void createArticleDelegatesToService() throws Exception {
        ArticleDTO dto = new ArticleDTO();
        dto.setId("art-1");
        when(articleService.save(any(ArticleCreationDTO.class))).thenReturn(dto);

        ArticleCreationDTO request = new ArticleCreationDTO();
        request.setTitle("New Article");

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/articles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("art-1"));

        verify(articleService).save(any(ArticleCreationDTO.class));
    }

    @Test
    void updateArticleDelegatesToService() throws Exception {
        ArticleDTO dto = new ArticleDTO();
        dto.setId("art-2");
        when(articleService.update(any(ArticleUpdateDTO.class))).thenReturn(dto);

        ArticleUpdateDTO request = new ArticleUpdateDTO();
        request.setId("art-2");
        request.setTitle("Updated");

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/articles/{id}", "art-2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("art-2"));

        verify(articleService).update(any(ArticleUpdateDTO.class));
    }

    @Test
    void deleteArticleReturnsOk() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/articles/{id}", "art-3"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(articleService).delete("art-3");
    }

    @Test
    void getArticleByIdReturnsDto() throws Exception {
        ArticleDTO dto = new ArticleDTO();
        dto.setId("art-4");
        when(articleService.getDTOById("art-4")).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/articles/{id}", "art-4"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("art-4"));
    }

    @Test
    void searchArticlesReturnsPage() throws Exception {
        Page<ArticleDTO> page = new Page<>(1, 10);
        when(articleService.search(eq(1), eq(10), any())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/articles"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(articleService).search(eq(1), eq(10), any());
    }

    @Test
    void updateVisibilityDelegatesToService() throws Exception {
        ArticleDTO dto = new ArticleDTO();
        dto.setId("art-5");
        when(articleService.updateVisibility(eq("art-5"), any(ContentVisibilityUpdateDTO.class))).thenReturn(dto);

        ContentVisibilityUpdateDTO request = ContentVisibilityUpdateDTO.builder().id("art-5").visibility(com.stan.blog.beans.consts.Const.Visibility.PUBLIC).build();

        mockMvc.perform(MockMvcRequestBuilders.put("/v1/articles/{id}/visibility", "art-5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value("art-5"));

        verify(articleService).updateVisibility(eq("art-5"), any(ContentVisibilityUpdateDTO.class));
    }
}


