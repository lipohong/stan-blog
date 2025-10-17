package com.stan.blog.core.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stan.blog.beans.dto.user.EnhancedUserDetail;
import com.stan.blog.beans.dto.user.UserLoginDTO;
import com.stan.blog.beans.entity.user.UserEntity;
import com.stan.blog.core.exception.EmailNotVerifiedException;
import com.stan.blog.core.service.AuthService;
import com.stan.blog.core.service.UserService;
import com.stan.blog.core.utils.TokenUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final TokenUtil tokenUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<EnhancedUserDetail> login(@RequestBody final UserLoginDTO dto) {
        UserEntity userEntity = userService.findByUsernameOrEmailOrPhone(dto.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Invalid user name[ " + dto.getUsername() + " ], please confirm again!"));

        if (!Boolean.TRUE.equals(userEntity.getEmailVerified())) {
            throw new EmailNotVerifiedException(
                    "Email not verified. Please check your email and complete the verification process.");
        }

        authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword()));
        return ResponseEntity.ok(authService.login(dto));
    }

    @GetMapping("/refresh")
    public ResponseEntity<String> refresh(final HttpServletRequest request) {
        final String refreshToken = request.getHeader(TokenUtil.REFRESH_KEY);
        if (StringUtils.isBlank(refreshToken)) {
            return ResponseEntity.ok(null);
        }
        return ResponseEntity
                .ok(tokenUtil.freshAccessToken(refreshToken.substring(TokenUtil.TOKEN_PREFIX.length())));
    }
}