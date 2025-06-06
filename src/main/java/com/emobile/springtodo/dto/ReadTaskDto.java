package com.emobile.springtodo.dto;

import java.time.LocalDateTime;

public record ReadTaskDto(Long id, Long userId, String title, String description, LocalDateTime createAt, LocalDateTime dueDate, Boolean completed) {
}
