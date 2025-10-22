package com.stan.blog.core.utils;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.stan.blog.beans.dto.user.EnhancedUserDetail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SecurityUtil {

    public static Long getUserId() {
        return getUserDetail() != null ? getUserDetail().getUserProfile().getId() : -1;
    }

    public static String getUsername() {
        return getUserDetail() != null ? getUserDetail().getUsername() : "Unknown";
    }

    @SuppressWarnings("unchecked")
    public static List<GrantedAuthority> getAuthorities() {
        return getUserDetail() != null ? (List<GrantedAuthority>) getUserDetail().getAuthorities() : List.of();
    }

    public static void setUserDetail(Authentication auth) {
        try {
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ex) {
            log.error("Cannot set user detail into the security context", ex);
        }
    }

    public static EnhancedUserDetail getCurrentUserDetail() {
        return getUserDetail();
    }

    private static EnhancedUserDetail getUserDetail() {
        try {
            var context = SecurityContextHolder.getContext();
            var authentication = context != null ? context.getAuthentication() : null;
            if (authentication == null) {
                log.debug("No authentication present in security context");
                return null;
            }
            Object principal = authentication.getPrincipal();
            if (principal instanceof EnhancedUserDetail userDetail) {
                return userDetail;
            }
            if (principal == null) {
                log.debug("Authentication principal is null");
            } else {
                log.debug("Principal is not EnhancedUserDetail: {}", principal.getClass().getName());
            }
        } catch (Exception ex) {
            log.warn("Cannot get user detail from the security context", ex);
        }
        return null;
    }
}
