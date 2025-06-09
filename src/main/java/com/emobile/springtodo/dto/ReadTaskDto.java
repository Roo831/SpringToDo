package com.emobile.springtodo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ReadTaskDto(

        @Schema(description = "ID задачи", example = "10")
        Long id,

        @Schema(description = "ID пользователя", example = "1")
        Long userId,

        @Schema(description = "Заголовок задачи", example = "Написать тесты")
        String title,

        @Schema(description = "Описание задачи", example = "Покрыть TaskService unit-тестами")
        String description,

        @Schema(description = "Дата создания", example = "2025-06-01T09:00:00")
        LocalDateTime createAt,

        @Schema(description = "Дедлайн", example = "2025-06-10T17:00:00")
        LocalDateTime dueDate,

        @Schema(description = "Завершена ли задача", example = "false")
        Boolean completed
) {
}
