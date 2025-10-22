package com.stan.blog.ai.service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.Profile;

import com.stan.blog.ai.dto.QuotaResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class AiService {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${spring.ai.openai.api-key:}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:deepseek-chat}")
    private String model;

    @Value("${ai.title.daily-limit:5}")
    private int dailyLimit;

    @Value("${ai.title.temperature:0.2}")
    private double temperature;

    @Value("${ai.title.max-tokens:128}")
    private int maxTokens;

    @Value("${ai.title.prompt:}")
    private String systemPrompt;

    private final RestTemplate restTemplate = new RestTemplate();

    public QuotaResponse checkQuota(Long userId, boolean isAdmin) {
        if (isAdmin) {
            QuotaResponse resp = new QuotaResponse();
            resp.setAllowed(true);
            resp.setRemaining(Integer.MAX_VALUE);
            return resp;
        }
        int used = getUsage(userId);
        int remaining = Math.max(dailyLimit - used, 0);
        QuotaResponse resp = new QuotaResponse();
        resp.setAllowed(remaining > 0);
        resp.setRemaining(remaining);
        return resp;
    }

    public String generateTitle(String content) {
        String url = StringUtils.appendIfMissing(baseUrl, "/") + "v1/chat/completions";
        
        Map<String, Object> payload = new HashMap<>();

        payload.put("model", model);
        payload.put("messages", List.of(
                Map.of("role", "system", "content", this.systemPrompt),
                Map.of("role", "user", "content", content)
        ));
        payload.put("temperature", this.temperature);
        payload.put("max_tokens", this.maxTokens);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                Map.class
        );

        Map body = response.getBody();
        if (body == null) {
            throw new RuntimeException("Empty response from AI service");
        }
        Object choicesObj = body.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
            throw new RuntimeException("Invalid AI response: choices missing");
        }
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> firstMap)) {
            throw new RuntimeException("Invalid AI response format");
        }
        Object messageObj = firstMap.get("message");
        if (!(messageObj instanceof Map<?, ?> message)) {
            throw new RuntimeException("Invalid AI response: message missing");
        }
        Object contentObj = message.get("content");
        if (!(contentObj instanceof String raw)) {
            throw new RuntimeException("Invalid AI response: content missing");
        }
        return sanitizeTitle(raw);
    }

    public void increaseUsage(Long userId) {
        String key = getRedisKey(userId);
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, Duration.ofHours(24));
        }
    }

    private int getUsage(Long userId) {
        String key = getRedisKey(userId);
        String val = stringRedisTemplate.opsForValue().get(key);
        if (StringUtils.isBlank(val)) {
            return 0;
        }
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException ex) {
            log.warn("Invalid usage value in Redis for key {}: {}", key, val);
            return 0;
        }
    }

    private String getRedisKey(Long userId) {
        return "ai:title:" + LocalDate.now() + ":" + userId;
    }

    private static String sanitizeTitle(String raw) {
        String s = raw.trim();
        // Remove surrounding quotes
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1);
        }
        s = s.replaceAll("\r?\n", " ").trim();
        if (s.length() > 128) {
            s = s.substring(0, 128);
        }
        return s;
    }
}