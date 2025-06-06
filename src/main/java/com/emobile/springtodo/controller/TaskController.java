package com.emobile.springtodo.controller;


import com.emobile.springtodo.dto.CreateTaskDto;
import com.emobile.springtodo.dto.ReadTaskDto;
import com.emobile.springtodo.dto.UpdateTaskDto;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<List<ReadTaskDto>> getAllTasks(@AuthenticationPrincipal User user) {

        return ResponseEntity.ok(taskService.getAllUserTasks(user));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReadTaskDto> getTaskById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(taskService.getTaskById(id, user));
    }

    @PostMapping
    public ResponseEntity<ReadTaskDto> createTask(
            @Valid @RequestBody CreateTaskDto task,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(task, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReadTaskDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskDto task,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.updateTask(id, task, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        taskService.deleteTask(id, user);
        return ResponseEntity.noContent().build();
    }
}