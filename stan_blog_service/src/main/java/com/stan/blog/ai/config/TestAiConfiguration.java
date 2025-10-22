package com.stan.blog.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import com.stan.blog.ai.service.AiService;

@Configuration
@Profile("test")
public class TestAiConfiguration {

    // Provide a lightweight RedisConnectionFactory for tests
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory();
    }

    // Provide StringRedisTemplate for AiService in tests
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

    @Bean
    public AiService aiService(StringRedisTemplate stringRedisTemplate) {
        return new AiService(stringRedisTemplate);
    }
}