package com.stan.blog.core.configuration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;

import com.stan.blog.core.service.EmailService;

@Configuration
@Profile("test")
public class TestRedisConfiguration {

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        return Mockito.mock(RedisTemplate.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Bean
    public JavaMailSender javaMailSender() {
        return Mockito.mock(JavaMailSender.class);
    }

    @Bean
    public EmailService emailService() {
        return Mockito.mock(EmailService.class);
    }
}