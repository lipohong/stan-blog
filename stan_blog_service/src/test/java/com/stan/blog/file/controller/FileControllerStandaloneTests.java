package com.stan.blog.file.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.stan.blog.beans.consts.Const;
import com.stan.blog.beans.dto.file.FileResourceDTO;
import com.stan.blog.beans.dto.user.EnhancedUserDetail;
import com.stan.blog.beans.dto.user.UserGeneralDTO;
import com.stan.blog.beans.entity.file.FileResourceEntity;
import com.stan.blog.file.service.FileResourceService;

public class FileControllerStandaloneTests {

    private MockMvc mockMvc;
    private FileResourceService fileService;

    private EnhancedUserDetail user1;
    private EnhancedUserDetail user2;

    @BeforeEach
    void setup() {
        fileService = Mockito.mock(FileResourceService.class);
        FileController controller = new FileController(fileService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        user1 = buildUser(1L, "user1", List.of(Const.Role.BASIC.getValue()));
        user2 = buildUser(2L, "user2", List.of(Const.Role.BASIC.getValue()));
    }

    private EnhancedUserDetail buildUser(Long id, String username, List<String> roles) {
        UserGeneralDTO profile = new UserGeneralDTO();
        profile.setId(id);
        profile.setUsername(username);
        var authorities = roles.stream().map(SimpleGrantedAuthority::new).toList();
        return new EnhancedUserDetail(authorities, username, "pwd", profile);
    }

    private RequestPostProcessor withAuth(EnhancedUserDetail principal) {
        return request -> {
            var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
            return request;
        };
    }

    @Test
    @DisplayName("Upload: missing file returns 400")
    void upload_missingFile_returns400() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/v1/files/upload")
                .param("publicToAll", "true")
                .principal(() -> user1.getUsername()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Upload: successful returns DTO")
    void upload_success_returnsDTO() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "hello.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8));
        FileResourceDTO dto = new FileResourceDTO();
        dto.setId(100L);
        dto.setOriginalFilename("hello.txt");
        dto.setDownloadUrl("/v1/files/100/download");
        when(fileService.upload(any(), any(Boolean.class))).thenReturn(dto);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/v1/files/upload")
                .file(file)
                .param("publicToAll", "true")
                .with(withAuth(user1)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.downloadUrl", containsString("/v1/files/100/download")));
    }

    @Test
    @DisplayName("View: inline returns bytes and inline disposition")
    void view_inline_ok() throws Exception {
        when(fileService.viewInline(100L)).thenReturn(
                org.springframework.http.ResponseEntity.ok()
                        .header("Content-Disposition", "inline; filename=\"hello.txt\"")
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("hello".getBytes(StandardCharsets.UTF_8))
        );

        mockMvc.perform(get("/v1/files/{id}/view", 100))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("inline")))
                .andExpect(content().contentType("text/plain"))
                .andExpect(content().string("hello"));
    }

    @Test
    @DisplayName("Visibility: owner can update, others get 401")
    void visibility_update_permissions() throws Exception {
        FileResourceEntity entity = new FileResourceEntity();
        entity.setId(200L);
        entity.setOwnerId(1L);
        entity.setPublicToAll(false);

        when(fileService.getById(200L)).thenReturn(entity);
        when(fileService.canModify(entity)).thenReturn(true).thenReturn(false);
        when(fileService.toDTO(any())).thenAnswer(inv -> {
            FileResourceEntity e = inv.getArgument(0);
            FileResourceDTO d = new FileResourceDTO();
            d.setId(e.getId());
            d.setPublicToAll(e.getPublicToAll());
            return d;
        });

        // owner updates -> 200
        mockMvc.perform(put("/v1/files/{id}/visibility", 200)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"publicToAll\":true}")
                .with(withAuth(user1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicToAll", is(true)));

        // others -> 401
        mockMvc.perform(put("/v1/files/{id}/visibility", 200)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"publicToAll\":true}")
                .with(withAuth(user2)))
                .andExpect(status().isUnauthorized());
    }
}
