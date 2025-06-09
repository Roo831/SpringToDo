package com.emobile.springtodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
@Schema(description = "Запрос на создание новой задачи")
public record CreateTaskDto(
        @Schema(description = "Заголовок задачи", example = "Закончить проект")
        @NotBlank
        @Size(max = 255)
        String title,

        @Schema(description = "Описание задачи", example = "Закончить бэкенд и покрыть тестами")
        @Size(max = 1000)
        String description,

        @Schema(description = "Дата дедлайна", example = "2025-06-15T18:00:00")
        @NotNull
        @Future
        LocalDateTime dueDate) {
}
