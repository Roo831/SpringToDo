package com.emobile.springtodo.service;

import com.emobile.springtodo.dto.AuthRequest;
import com.emobile.springtodo.dto.AuthResponse;
import com.emobile.springtodo.dto.RegisterDto;
import com.emobile.springtodo.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;


@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserServiceImpl userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldReturnToken() {
        RegisterDto request = new RegisterDto("user@example.com", "password123");
        User mockUser = new User();
        mockUser.setUsername("user@example.com");

        when(userService.createUser(request)).thenReturn(mockUser);
        when(jwtService.generateToken(mockUser)).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("jwt-token", response.token());
        verify(userService).createUser(request);
        verify(jwtService).generateToken(mockUser);
    }

    @Test
    void authenticate_shouldReturnToken() {
        AuthRequest request = new AuthRequest("user@example.com", "password123");
        User mockUser = new User();
        mockUser.setUsername("user@example.com");

        when(userService.loadUserByUsername(request.email())).thenReturn(mockUser);
        when(jwtService.generateToken(mockUser)).thenReturn("jwt-token");

        AuthResponse response = authService.authenticate(request);

        assertEquals("jwt-token", response.token());
        verify(authenticationManager).authenticate(any());
    }
}
