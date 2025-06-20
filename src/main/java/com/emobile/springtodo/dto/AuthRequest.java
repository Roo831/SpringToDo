package com.emobile.springtodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос на аутентификацию")
public record AuthRequest(
        @Email
        @NotBlank
        String email,

        @Schema(description = "Пароль (минимум 8 символов)", example = "password123")
        @NotBlank
        @Size(min = 8)
        String password
) {}