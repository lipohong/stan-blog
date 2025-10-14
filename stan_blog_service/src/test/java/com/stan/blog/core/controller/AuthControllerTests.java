package com.stan.blog.core.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stan.blog.beans.dto.user.EnhancedUserDetail;
import com.stan.blog.beans.dto.user.UserLoginDTO;
import com.stan.blog.beans.dto.user.UserGeneralDTO;
import com.stan.blog.beans.entity.user.UserEntity;
import com.stan.blog.core.exception.EmailNotVerifiedException;
import com.stan.blog.core.service.AuthService;
import com.stan.blog.core.service.UserService;
import com.stan.blog.core.utils.TokenUtil;

class AuthControllerTests {

    private MockMvc mockMvc;
    private AuthService authService;
    private UserService userService;
    private TokenUtil tokenUtil;
    private AuthenticationManager authenticationManager;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        userService = mock(UserService.class);
        tokenUtil = mock(TokenUtil.class);
        authenticationManager = mock(AuthenticationManager.class);
        AuthController controller = new AuthController(authService, userService, tokenUtil, authenticationManager);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void loginReturnsEnhancedUserDetailWhenCredentialsValid() throws Exception {
        UserEntity entity = new UserEntity();
        entity.setEmailVerified(true);
        when(userService.getOne(any())).thenReturn(entity);
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        EnhancedUserDetail detail = new EnhancedUserDetail(null, "stan", null, new UserGeneralDTO());
        when(authService.login(any(UserLoginDTO.class))).thenReturn(detail);

        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("stan");
        dto.setPassword("pwd");

        mockMvc.perform(MockMvcRequestBuilders.post("/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.username").value("stan"));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(authService).login(any(UserLoginDTO.class));
    }

    @Test
    void loginThrowsWhenEmailNotVerified() {
        UserEntity entity = new UserEntity();
        entity.setEmailVerified(false);
        when(userService.getOne(any())).thenReturn(entity);
        AuthController controller = new AuthController(authService, userService, tokenUtil, authenticationManager);
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("stan");
        dto.setPassword("pwd");

        assertThrows(EmailNotVerifiedException.class, () -> controller.login(dto));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void refreshReturnsNullWhenNoHeader() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/v1/auth/refresh"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(""));
    }

    @Test
    void refreshReturnsNewTokenWhenHeaderProvided() throws Exception {
        when(tokenUtil.freshAccessToken("refresh-token")).thenReturn("new-access");

        mockMvc.perform(MockMvcRequestBuilders.get("/v1/auth/refresh")
                .header(TokenUtil.REFRESH_KEY, TokenUtil.TOKEN_PREFIX + "refresh-token"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("new-access"));

        verify(tokenUtil).freshAccessToken("refresh-token");
    }
}
