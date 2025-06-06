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
class AuthServiceTest { // TODO: переписать с spring security test

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
        RegisterDto request = new RegisterDto("user@example.com", "password123"); // создаем дто
        User mockUser = new User(); // // создаем пользователя
        mockUser.setUsername("user@example.com"); // создаем пользователя почему то только емейл ставим, без пароля

        when(userService.createUser(request)).thenReturn(mockUser); // мок бины, вызываемые в реальном классе должны создавать нашего мок юзера, когда мы посылаем ему dto
        when(jwtService.generateToken(mockUser)).thenReturn("jwt-token"); // мок бин возвращает токен jwt-token

        AuthResponse response = authService.register(request);  // получаем ответ от нашего бина

        assertEquals("jwt-token", response.token()); // сравниваем, токен ли вернулся
        verify(userService).createUser(request); // сверяем, что сработал метод createUser
        verify(jwtService).generateToken(mockUser); // сверяем, что сработал метод generateToken
    }

    @Test
    void authenticate_shouldReturnToken() {
        AuthRequest request = new AuthRequest("user@example.com", "password123"); // Всё то же самое
        User mockUser = new User(); // Всё то же самое
        mockUser.setUsername("user@example.com"); // Всё то же самое

        when(userService.loadUserByUsername(request.email())).thenReturn(mockUser); // когда наш сервис вызывает loadUserByUsername, возвращаем ему мок юзера
        when(jwtService.generateToken(mockUser)).thenReturn("jwt-token");  // мок бин возвращает токен jwt-token

        AuthResponse response = authService.authenticate(request); // получаем ответ от нашего бина

        assertEquals("jwt-token", response.token()); // сравниваем
        verify(authenticationManager).authenticate(any()); // проверяем, чтобы authenticateManager... я не знаю что это
    }
}
