package com.emobile.springtodo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserDto( @Email(message = "Invalid email format") String email, @Size(min = 8)  String password) {
}
