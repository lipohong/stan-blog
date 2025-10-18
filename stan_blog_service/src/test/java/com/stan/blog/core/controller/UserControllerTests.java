package com.stan.blog.core.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import com.stan.blog.beans.dto.user.UserCreationDTO;
import com.stan.blog.beans.dto.user.UserFeatureDTO;
import com.stan.blog.beans.dto.user.UserGeneralDTO;
import com.stan.blog.beans.dto.user.UserUpdateDTO;
import com.stan.blog.core.service.UserService;

class UserControllerTests {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = org.mockito.Mockito.mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void getUsersUsesServiceWithProvidedFilters() {
        Page<UserGeneralDTO> page = new PageImpl<>(java.util.List.of(), PageRequest.of(1, 10), 0);
        when(userService.getUsers(2, 5, "stan", Boolean.TRUE)).thenReturn(page);

        var response = userController.getUsers(2, 5, "stan", Boolean.TRUE);

        assertEquals(2L, response.getBody().getCurrent());
        assertEquals(10L, response.getBody().getSize());
        assertEquals(0L, response.getBody().getTotal());
        assertEquals(0, response.getBody().getPages());
        assertTrue(response.getBody().getRecords().isEmpty());
        verify(userService).getUsers(2, 5, "stan", Boolean.TRUE);
    }

    @Test
    void createUserReturnsCreatedProfile() {
        UserGeneralDTO dto = new UserGeneralDTO();
        dto.setId(10L);
        when(userService.createUser(any(UserCreationDTO.class))).thenReturn(dto);

        UserCreationDTO request = new UserCreationDTO();
        request.setEmail("user@example.com");
        request.setPassword("secret1");

        var response = userController.createUser(request);

        assertEquals(10L, response.getBody().getId());
        verify(userService).createUser(any(UserCreationDTO.class));
    }

    @Test
    void updateUserCallsService() {
        UserGeneralDTO dto = new UserGeneralDTO();
        dto.setId(5L);
        when(userService.updateUser(any(UserUpdateDTO.class))).thenReturn(dto);

        UserUpdateDTO request = new UserUpdateDTO();
        request.setId(5L);
        request.setFeatures(new UserFeatureDTO(true, false, true, true));

        var response = userController.updateUser(5L, request);

        assertEquals(5L, response.getBody().getId());
        verify(userService).updateUser(any(UserUpdateDTO.class));
    }

    @Test
    void deleteUserDelegatesToService() {
        userController.deleteUser(7L);

        verify(userService).deleteUser(7L);
    }

    @Test
    void updateUserRolesDelegatesToService() {
        UserGeneralDTO dto = new UserGeneralDTO();
        dto.setId(8L);
        when(userService.updateUserRoles(eq(8L), any())).thenReturn(dto);

        var response = userController.updateUserRoles(8L, List.of("ROLE_ADMIN"));

        assertEquals(8L, response.getBody().getId());
        verify(userService).updateUserRoles(eq(8L), eq(List.of("ROLE_ADMIN")));
    }
}
