package com.emobile.springtodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateTaskDto(
        @Schema(description = "Новый заголовок задачи", example = "Обновлённое имя задачи")
        @Size(max = 255)
        String title,

        @Schema(description = "Новое описание задачи", example = "Новое описание задачи")
        @Size(max = 1000)
        String description,

        @Schema(description = "Новая дата дедлайна", example = "2025-06-20T23:59:00")
        @Future
        LocalDateTime dueDate,

        @Schema(description = "Флаг завершения", example = "true")
        Boolean isCompleted

) {}
