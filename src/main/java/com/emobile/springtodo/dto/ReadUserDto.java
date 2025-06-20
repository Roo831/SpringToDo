package com.emobile.springtodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ReadUserDto(
        @Schema(description = "ID пользователя", example = "1")
        Long id,

        @Schema(description = "Email пользователя", example = "user@example.com")
        String email,

        @Schema(description = "Дата создания аккаунта", example = "2024-05-30T10:15:30")
        LocalDateTime createdAt
) {

}
