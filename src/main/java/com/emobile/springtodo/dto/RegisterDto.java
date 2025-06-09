package com.emobile.springtodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record RegisterDto(

        @Schema(description = "Email пользователя", example = "newuser@example.com")
        @Email
        @NotBlank
        String email,

        @Schema(description = "Пароль (минимум 8 символов)", example = "strongPassword123")
        @NotBlank
        @Size(min = 8)
        String password
) {}