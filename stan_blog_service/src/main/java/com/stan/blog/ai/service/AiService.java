package com.stan.blog.ai.service;

import java.time.Duration;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.stan.blog.ai.dto.QuotaResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!test")
@RequiredArgsConstructor
public class AiService {

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${ai.title.daily-limit:5}")
    private int dailyLimit;

    @Value("${ai.title.prompt:}")
    private String systemPrompt;

    private final ChatClient chatClient;

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
        String aiOutput = chatClient
                .prompt()
                .system(this.systemPrompt)
                .user(content)
                .call()
                .content();
        return aiOutput.replace('\r', ' ')
                       .replace('\n', ' ')
                       .replaceAll("\\s+", " ")
                       .trim();
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

}