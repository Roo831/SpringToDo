package com.emobile.springtodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserDto(

        @Schema(description = "Новый email", example = "newemail@example.com")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Новый пароль", example = "newSecurePassword123")
        @Size(min = 8)
        String password) {

}
