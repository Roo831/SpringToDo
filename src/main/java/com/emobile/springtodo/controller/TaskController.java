package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.CreateTaskDto;
import com.emobile.springtodo.dto.ReadTaskDto;
import com.emobile.springtodo.dto.UpdateTaskDto;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.service.TaskService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Управление задачами пользователя")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Timed(value = "tasks.getAll.time", description = "Время получения всех задач")
    @Counted(value = "tasks.getAll.count", description = "Количество запросов на получение всех задач")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Получить все задачи пользователя")
    public List<ReadTaskDto> getAllTasks(@AuthenticationPrincipal User user) {
        return taskService.getAllUserTasks(user);
    }

    @Timed(value = "tasks.getPaged.time", description = "Время получения задач с пагинацией")
    @Counted(value = "tasks.getPaged.count", description = "Количество запросов с пагинацией")
    @GetMapping("/paged")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Получить задачи с пагинацией")
    public List<ReadTaskDto> getTasksPaged(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        return taskService.getUserTasksWithPagination(user, limit, offset);
    }

    @Timed(value = "tasks.getById.time", description = "Время получения задачи по ID")
    @Counted(value = "tasks.getById.count", description = "Количество запросов задачи по ID")
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Получить задачу по ID")
    public ReadTaskDto getTaskById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return taskService.getTaskById(id, user);
    }

    @Timed(value = "tasks.create.time", description = "Время создания задачи")
    @Counted(value = "tasks.create.count", description = "Количество созданных задач")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать новую задачу")
    public ReadTaskDto createTask(
            @Valid @RequestBody CreateTaskDto task,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return taskService.createTask(task, user);
    }

    @Timed(value = "tasks.update.time", description = "Время обновления задачи")
    @Counted(value = "tasks.update.count", description = "Количество обновлений задачи")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Обновить задачу по ID")
    public ReadTaskDto updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskDto task,
            @AuthenticationPrincipal User user) {
        return taskService.updateTask(id, task, user);
    }

    @Timed(value = "tasks.delete.time", description = "Время удаления задачи")
    @Counted(value = "tasks.delete.count", description = "Количество удалений задач")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить задачу по ID")
    public void deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        taskService.deleteTask(id, user);
    }
}