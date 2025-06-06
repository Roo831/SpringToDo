package com.emobile.springtodo.dto;

import java.time.LocalDateTime;

public record ReadUserDto(Long id, String email, LocalDateTime createdAt) {

}
