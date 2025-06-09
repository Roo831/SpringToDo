package com.emobile.springtodo.controller;


import com.emobile.springtodo.dto.AuthRequest;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.annotation.Counted;
import com.emobile.springtodo.dto.AuthResponse;
import com.emobile.springtodo.dto.RegisterDto;
import com.emobile.springtodo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Регистрация и вход пользователя")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Timed(value = "auth.register.time", description = "Время регистрации пользователя")
    @Counted(value = "auth.register.count", description = "Количество регистраций пользователей")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Регистрация нового пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Неверные данные")
    })
    public AuthResponse register(@Valid @RequestBody RegisterDto request) {
        return authService.register(request);
    }

    @Timed(value = "auth.login.time", description = "Время аутентификации пользователя")
    @Counted(value = "auth.login.count", description = "Количество логинов")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Аутентификация пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешная аутентификация"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.authenticate(request);
    }
}