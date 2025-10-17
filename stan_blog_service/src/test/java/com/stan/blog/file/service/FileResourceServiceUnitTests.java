package com.stan.blog.file.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.stan.blog.beans.dto.user.EnhancedUserDetail;
import com.stan.blog.beans.dto.user.UserGeneralDTO;
import com.stan.blog.beans.entity.file.FileResourceEntity;
import com.stan.blog.beans.repository.file.FileResourceRepository;
import com.stan.blog.file.service.storage.StorageProperties;
import com.stan.blog.file.service.storage.StorageService;

class FileResourceServiceUnitTests {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private FileResourceService newService() {
        StorageService storage = org.mockito.Mockito.mock(StorageService.class);
        StorageProperties props = new StorageProperties();
        FileResourceRepository repo = org.mockito.Mockito.mock(FileResourceRepository.class);
        return new FileResourceService(storage, props, repo);
    }

    private void setPrincipal(long userId, boolean admin) {
        UserGeneralDTO profile = new UserGeneralDTO();
        profile.setId(userId);
        var authorities = admin ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN")) : List.of(new SimpleGrantedAuthority("ROLE_BASIC"));
        EnhancedUserDetail principal = new EnhancedUserDetail(authorities, "u"+userId, "p", profile);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("canModify: owner allowed")
    void canModify_owner() {
        var service = newService();
        FileResourceEntity e = new FileResourceEntity();
        e.setOwnerId(10L);
        setPrincipal(10L, false);
        assertTrue(service.canModify(e));
    }

    @Test
    @DisplayName("canModify: admin allowed")
    void canModify_admin() {
        var service = newService();
        FileResourceEntity e = new FileResourceEntity();
        e.setOwnerId(10L);
        setPrincipal(99L, true);
        assertTrue(service.canModify(e));
    }

    @Test
    @DisplayName("canModify: non-owner non-admin denied")
    void canModify_denied() {
        var service = newService();
        FileResourceEntity e = new FileResourceEntity();
        e.setOwnerId(10L);
        setPrincipal(11L, false);
        assertFalse(service.canModify(e));
    }
}

