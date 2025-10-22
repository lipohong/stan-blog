package com.stan.blog.ai.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stan.blog.ai.dto.QuotaResponse;
import com.stan.blog.ai.dto.TitleGenerateRequest;
import com.stan.blog.ai.service.AiService;
import com.stan.blog.beans.dto.user.EnhancedUserDetail;
import com.stan.blog.core.dto.GenericResponse;
import com.stan.blog.core.utils.AuthenticationUtil;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @GetMapping("/check-quota")
    public ResponseEntity<GenericResponse<QuotaResponse>> checkQuota() {
        return AuthenticationUtil.withAuthenticatedUser(user -> {
            boolean isAdmin = hasAdmin(user);
            QuotaResponse quota = aiService.checkQuota(user.getUserProfile().getId(), isAdmin);
            return ResponseEntity.ok(GenericResponse.ok(quota));
        });
    }

    @PostMapping("/generate-title")
    public ResponseEntity<GenericResponse<String>> generateTitle(@RequestBody TitleGenerateRequest request) {
        return AuthenticationUtil.withAuthenticatedUser(user -> {
            String content = StringUtils.defaultString(request.getContent()).trim();
            if (content.length() <= 100) {
                return ResponseEntity.ok(GenericResponse.fail("Content too short, must be > 100 characters"));
            }
            if (content.length() > 5000) {
                return ResponseEntity.ok(GenericResponse.fail("Content too long, must be ≤ 5000 characters"));
            }

            boolean isAdmin = hasAdmin(user);
            QuotaResponse quota = aiService.checkQuota(user.getUserProfile().getId(), isAdmin);
            if (!isAdmin && !quota.isAllowed()) {
                return ResponseEntity.ok(GenericResponse.fail("Daily quota exhausted"));
            }

            String title = aiService.generateTitle(content);
            if (StringUtils.isBlank(title)) {
                return ResponseEntity.ok(GenericResponse.fail("Title generation failed"));
            }
            if (!isAdmin) {
                aiService.increaseUsage(user.getUserProfile().getId());
            }
            return ResponseEntity.ok(GenericResponse.ok(title));
        });
    }

    private boolean hasAdmin(EnhancedUserDetail user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> StringUtils.containsIgnoreCase(auth.getAuthority(), "ADMIN"));
    }
}