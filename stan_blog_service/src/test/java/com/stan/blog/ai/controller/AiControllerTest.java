package com.stan.blog.ai.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stan.blog.ai.dto.QuotaResponse;
import com.stan.blog.ai.dto.TitleGenerateRequest;
import com.stan.blog.ai.service.AiService;
import com.stan.blog.beans.dto.user.EnhancedUserDetail;
import com.stan.blog.beans.dto.user.UserGeneralDTO;
import com.stan.blog.core.utils.SecurityUtil;

@WebMvcTest(controllers = AiController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = "security-context.enable=false")
public class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiService aiService;

    @Mock
    private GrantedAuthority mockAuthority;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();
    }

    private EnhancedUserDetail authenticate(long id, String username, boolean admin) {
        UserGeneralDTO profile = new UserGeneralDTO();
        profile.setId(id);
        profile.setUsername(username);
        List<GrantedAuthority> roles = List.of(new SimpleGrantedAuthority(admin ? "ROLE_ADMIN" : "ROLE_USER"));
        EnhancedUserDetail principal = new EnhancedUserDetail(roles, username, "", profile);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(principal, null, roles);
        SecurityUtil.setUserDetail(auth);
        return principal;
    }

    @Test
    @DisplayName("check-quota: 401 when unauthenticated")
    void checkQuota_unauthenticated() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/v1/ai/check-quota"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("check-quota: returns allowed and remaining for user")
    void checkQuota_ok() throws Exception {
        var user = authenticate(100L, "user", false);
        QuotaResponse qr = new QuotaResponse();
        qr.setAllowed(true);
        qr.setRemaining(2);
        when(aiService.checkQuota(eq(100L), eq(false))).thenReturn(qr);

        mockMvc.perform(get("/v1/ai/check-quota"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.allowed").value(true))
                .andExpect(jsonPath("$.data.remaining").value(2));
    }

    @Test
    @DisplayName("generate-title: content too short")
    void generateTitle_tooShort() throws Exception {
        authenticate(101L, "user", false);
        TitleGenerateRequest req = new TitleGenerateRequest();
        req.setContent("a".repeat(99));
        mockMvc.perform(post("/v1/ai/generate-title")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Content too short, must be > 100 characters"));
        verifyNoInteractions(aiService);
    }

    @Test
    @DisplayName("generate-title: content too long")
    void generateTitle_tooLong() throws Exception {
        authenticate(101L, "user", false);
        TitleGenerateRequest req = new TitleGenerateRequest();
        req.setContent("a".repeat(5001));
        mockMvc.perform(post("/v1/ai/generate-title")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Content too long, must be ≤ 5000 characters"));
        verifyNoInteractions(aiService);
    }

    @Test
    @DisplayName("generate-title: quota exhausted for non-admin")
    void generateTitle_quotaExhausted() throws Exception {
        authenticate(102L, "user", false);
        TitleGenerateRequest req = new TitleGenerateRequest();
        req.setContent("a".repeat(200));
        QuotaResponse qr = new QuotaResponse();
        qr.setAllowed(false);
        qr.setRemaining(0);
        when(aiService.checkQuota(eq(102L), eq(false))).thenReturn(qr);

        mockMvc.perform(post("/v1/ai/generate-title")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Daily quota exhausted"));
        verify(aiService, never()).generateTitle(anyString());
        verify(aiService, never()).increaseUsage(anyLong());
    }

    @Test
    @DisplayName("generate-title: success for non-admin and usage increased")
    void generateTitle_successNonAdmin() throws Exception {
        authenticate(103L, "user", false);
        TitleGenerateRequest req = new TitleGenerateRequest();
        req.setContent("a".repeat(200));
        QuotaResponse qr = new QuotaResponse();
        qr.setAllowed(true);
        qr.setRemaining(3);
        when(aiService.checkQuota(eq(103L), eq(false))).thenReturn(qr);
        when(aiService.generateTitle(anyString())).thenReturn("Nice Title");

        mockMvc.perform(post("/v1/ai/generate-title")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Nice Title"));
        verify(aiService, times(1)).increaseUsage(eq(103L));
    }

    @Test
    @DisplayName("generate-title: admin bypasses quota and no usage increment")
    void generateTitle_adminBypassesQuota() throws Exception {
        authenticate(104L, "admin", true);
        TitleGenerateRequest req = new TitleGenerateRequest();
        req.setContent("a".repeat(200));
        QuotaResponse qr = new QuotaResponse();
        qr.setAllowed(false);
        qr.setRemaining(0);
        when(aiService.checkQuota(eq(104L), eq(true))).thenReturn(qr);
        when(aiService.generateTitle(anyString())).thenReturn("Admin Title");

        mockMvc.perform(post("/v1/ai/generate-title")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Admin Title"));
        verify(aiService, never()).increaseUsage(anyLong());
    }

    @Test
    @DisplayName("generate-title: AI returns blank -> fail")
    void generateTitle_blankFail() throws Exception {
        authenticate(105L, "user", false);
        TitleGenerateRequest req = new TitleGenerateRequest();
        req.setContent("a".repeat(200));
        QuotaResponse qr = new QuotaResponse();
        qr.setAllowed(true);
        qr.setRemaining(3);
        when(aiService.checkQuota(eq(105L), eq(false))).thenReturn(qr);
        when(aiService.generateTitle(anyString())).thenReturn("   ");

        mockMvc.perform(post("/v1/ai/generate-title")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Title generation failed"));
        verify(aiService, never()).increaseUsage(anyLong());
    }
}