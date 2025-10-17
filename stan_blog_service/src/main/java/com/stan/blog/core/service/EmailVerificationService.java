package com.stan.blog.core.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stan.blog.beans.entity.user.UserEntity;
import com.stan.blog.core.exception.StanBlogRuntimeException;
import com.stan.blog.core.utils.CacheUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailService emailService;
    private final UserService userService;
    private final CacheUtil cacheUtil;

    @Value("")
    private String frontendUrl;

    private static final String EMAIL_VERIFICATION_TOKEN_KEY = "email_verification_token:";
    private static final String EMAIL_VERIFICATION_SUBJECT = "Verify Your Email Address";
    private static final String EMAIL_VERIFICATION_TEMPLATE = "email-verification";
    private static final int TOKEN_EXPIRE_MINUTES = 60;

    public void sendVerificationEmail(String email, String firstName) {
        try {
            String token = UUID.randomUUID().toString();
            cacheUtil.set(EMAIL_VERIFICATION_TOKEN_KEY + email, token, TOKEN_EXPIRE_MINUTES * 60);

            Map<String, Object> params = new HashMap<>();
            params.put("firstName", firstName != null ? firstName : "User");
            params.put("email", email);
            params.put("verificationLink", frontendUrl + "/verify-email?token=" + token + "&email=" + email);
            params.put("expirationMinutes", TOKEN_EXPIRE_MINUTES);

            emailService.sendTemplateEmail(email, EMAIL_VERIFICATION_SUBJECT, EMAIL_VERIFICATION_TEMPLATE, params);
            log.info("Verification email sent to: {}", email);
        } catch (Exception e) {
            log.error("Error sending verification email to: {}", email, e);
            throw new StanBlogRuntimeException("Failed to send verification email");
        }
    }

    @Transactional
    public Boolean verifyEmail(String email, String token) {
        try {
            String storedToken = (String) cacheUtil.get(EMAIL_VERIFICATION_TOKEN_KEY + email);

            if (storedToken == null) {
                log.warn("Email verification token expired or not found for email: {}", email);
                return Boolean.FALSE;
            }

            if (!storedToken.equals(token)) {
                log.warn("Invalid email verification token for email: {}", email);
                return Boolean.FALSE;
            }

            UserEntity user = userService.findByEmail(email).orElse(null);

            if (user == null) {
                log.warn("User not found for email: {}", email);
                return Boolean.FALSE;
            }

            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                log.info("Email already verified for: {}", email);
                return Boolean.TRUE;
            }

            user.setEmailVerified(Boolean.TRUE);
            userService.saveUser(user);
            cacheUtil.del(EMAIL_VERIFICATION_TOKEN_KEY + email);
            log.info("Email verified successfully for: {}", email);
            return Boolean.TRUE;
        } catch (Exception e) {
            log.error("Error verifying email for: {}", email, e);
            return Boolean.FALSE;
        }
    }

    public Boolean isEmailVerified(String email) {
        try {
            return userService.findByEmail(email)
                    .map(UserEntity::getEmailVerified)
                    .orElse(Boolean.FALSE);
        } catch (Exception e) {
            log.error("Error checking email verification status for: {}", email, e);
            return Boolean.FALSE;
        }
    }

    public void resendVerificationEmail(String email) {
        try {
            UserEntity user = userService.findByEmail(email).orElse(null);

            if (user == null) {
                throw new StanBlogRuntimeException("User not found");
            }

            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                throw new StanBlogRuntimeException("Email already verified");
            }

            sendVerificationEmail(email, user.getFirstName());

        } catch (StanBlogRuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error resending verification email to: {}", email, e);
            throw new StanBlogRuntimeException("Failed to resend verification email");
        }
    }
}