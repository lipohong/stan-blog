package com.stan.blog.content.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.eq;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stan.blog.DefaultTestData;
import com.stan.blog.beans.dto.content.CommentCreateDTO;
import com.stan.blog.beans.dto.content.CommentDTO;
import com.stan.blog.content.controller.CommentController;
import com.stan.blog.content.service.CommentService;
import com.stan.blog.core.utils.SecurityUtil;

class CommentControllerTests {

    private MockMvc mockMvc;
    private CommentService commentService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        commentService = org.mockito.Mockito.mock(CommentService.class);
        CommentController controller = new CommentController(commentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
        SecurityUtil.setUserDetail(DefaultTestData.getDefaultUserAuthentication());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createCommentRequiresAuthenticationAndDelegatesToService() throws Exception {
        CommentDTO dto = new CommentDTO();
        dto.setId(1L);
        when(commentService.createComment(any(CommentCreateDTO.class), any())).thenReturn(dto);

        CommentCreateDTO request = new CommentCreateDTO();
        request.setContent("Nice");
        request.setContentId("article-1");
        request.setContentType("ARTICLE");

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1));

        verify(commentService).createComment(any(CommentCreateDTO.class), any());
    }

    @Test
    void getCommentsReturnsPage() throws Exception {
        Page<CommentDTO> page = new PageImpl<>(java.util.List.of(), PageRequest.of(1, 10), 0);
        when(commentService.getCommentsForContent("c1", "ARTICLE", 1, 10)).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/comments")
                .param("contentId", "c1")
                .param("contentType", "ARTICLE"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(commentService).getCommentsForContent("c1", "ARTICLE", 1, 10);
    }

    @Test
    void deleteCommentCallsService() throws Exception {
        when(commentService.deleteComment(eq(5L), any())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.delete("/v1/comments/{id}", 5))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(commentService).deleteComment(eq(5L), any());
    }

    @Test
    void toggleLikeCallsService() throws Exception {
        when(commentService.toggleCommentLike(eq(6L), eq(true), any())).thenReturn(11L);

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/comments/{id}/like", 6)
                .param("isLike", "true"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("11"));

        verify(commentService).toggleCommentLike(eq(6L), eq(true), any());
    }
}


