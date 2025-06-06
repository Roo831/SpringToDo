package com.emobile.springtodo.dto;

import java.time.LocalDateTime;

public record UpdateTaskDto(String title, String description, LocalDateTime dueDate, Boolean isCompleted) {}
