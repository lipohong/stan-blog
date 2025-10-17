package com.stan.blog.core.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stan.blog.beans.dto.user.UserCreationDTO;
import com.stan.blog.beans.dto.user.UserGeneralDTO;
import com.stan.blog.beans.dto.user.UserUpdateDTO;
import com.stan.blog.core.dto.PageResponse;
import com.stan.blog.core.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<PageResponse<UserGeneralDTO>> getUsers(
            @RequestParam(value = "current", required = false, defaultValue = "1") int current,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "emailVerified", required = false) Boolean emailVerified) {
        return ResponseEntity.ok(PageResponse.from(userService.getUsers(current, size, keyword, emailVerified)));
    }

    @GetMapping("{id}")
    public ResponseEntity<UserGeneralDTO> getUser(@PathVariable long id) {
        return ResponseEntity.ok(userService.getUser(id));
    }

    @PostMapping
    public ResponseEntity<UserGeneralDTO> createUser(@RequestBody @Validated UserCreationDTO dto) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @PutMapping("{id}")
    public ResponseEntity<UserGeneralDTO> updateUser(
            @PathVariable long id,
            @RequestBody @Validated UserUpdateDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(userService.updateUser(dto));
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserGeneralDTO> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(null);
    }

    @PutMapping("{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<UserGeneralDTO> updateUserRoles(
            @PathVariable long id,
            @RequestBody java.util.List<String> roles) {
        return ResponseEntity.ok(userService.updateUserRoles(id, roles));
    }
}