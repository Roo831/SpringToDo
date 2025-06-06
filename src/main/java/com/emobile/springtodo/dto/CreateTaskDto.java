package com.emobile.springtodo.dto;

import java.time.LocalDateTime;

public record CreateTaskDto(String title, String description, LocalDateTime dueDate) {
}
