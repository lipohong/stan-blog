package com.stan.blog.core.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;

@Configuration
@Profile("test")
public class TestSecurityConfiguration {

    @Bean
    public AuthenticationManager authenticationManager() {
        return authentication -> {
            Object principal = authentication.getPrincipal();
            Object credentials = authentication.getCredentials();
            return new UsernamePasswordAuthenticationToken(
                principal,
                credentials,
                List.of(new SimpleGrantedAuthority("ROLE_BASIC"))
            );
        };
    }
}
