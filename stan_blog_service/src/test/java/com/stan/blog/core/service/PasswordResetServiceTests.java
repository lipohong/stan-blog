package com.stan.blog.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.stan.blog.core.utils.CacheUtil;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTests {

    @Mock
    private CacheUtil cacheUtil;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "https://frontend.example");
    }

    @Test
    void requestPasswordResetSkipsWhenEmailNotRegistered() {
        when(userService.isEmailRegistered("ghost@example.com")).thenReturn(false);

        passwordResetService.requestPasswordReset("ghost@example.com");

        verify(emailService, never()).sendTemplateEmail(any(), any(), any(), any());
        verify(cacheUtil, never()).set(any(), any());
    }

    @Test
    void requestPasswordResetGeneratesTokenAndSendsEmail() {
        String email = "user@example.com";
        when(userService.isEmailRegistered(email)).thenReturn(true);
        when(userService.getFirstNameByEmail(email)).thenReturn("Stan");
        when(cacheUtil.set(any(), any(), anyLong())).thenReturn(true);

        passwordResetService.requestPasswordReset(email);

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(cacheUtil).set(eq("PASSWORD_RESET_TOKEN_" + email), tokenCaptor.capture(), eq(300L));

        ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        verify(emailService).sendTemplateEmail(eq(email), eq("Password Reset - Stan Blog"), eq("password-reset-email"), paramsCaptor.capture());

        Map<String, Object> params = paramsCaptor.getValue();
        assertTrue(params.containsKey("resetUrl"));
        String resetUrl = params.get("resetUrl").toString();
        assertTrue(resetUrl.contains(tokenCaptor.getValue()));
        assertTrue(resetUrl.startsWith("https://frontend.example/reset-password"));
    }

    @Test
    void resetPasswordReturnsFalseWhenTokenInvalid() {
        when(cacheUtil.get("PASSWORD_RESET_TOKEN_user@example.com")).thenReturn(null);

        Boolean result = passwordResetService.resetPassword("user@example.com", "invalid", "newPass");

        assertFalse(result);
        verify(userService, never()).updatePassword(any(), any());
    }

    @Test
    void resetPasswordUpdatesPasswordAndClearsTokenWhenValid() {
        String email = "user@example.com";
        when(cacheUtil.get("PASSWORD_RESET_TOKEN_" + email)).thenReturn("token-123");
        when(userService.updatePassword(email, "newPass")).thenReturn(true);

        Boolean result = passwordResetService.resetPassword(email, "token-123", "newPass");

        assertTrue(result);
        verify(userService).updatePassword(email, "newPass");
        verify(cacheUtil).del("PASSWORD_RESET_TOKEN_" + email);
    }
}
