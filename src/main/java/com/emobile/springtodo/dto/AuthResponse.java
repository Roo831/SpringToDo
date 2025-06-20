package com.emobile.springtodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ при успешной аутентификации")
public record AuthResponse(
        @Schema(description = "JWT токен", example = "eyJhbGciOiJIUz...")
        String token
) {}