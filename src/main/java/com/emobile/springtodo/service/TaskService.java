package com.emobile.springtodo.service;


import com.emobile.springtodo.dto.CreateTaskDto;
import com.emobile.springtodo.dto.ReadTaskDto;
import com.emobile.springtodo.dto.UpdateTaskDto;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.exception.ResourceNotFoundException;
import com.emobile.springtodo.mapper.TaskMapper;
import com.emobile.springtodo.repository.TaskRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final Counter completedTasksCounter;

    @Autowired
    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper, MeterRegistry meterRegistry) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.completedTasksCounter = meterRegistry.counter("tasks.completed.count");
    }


    public ReadTaskDto createTask(CreateTaskDto task, User user) {
        Task newTask = Task.builder()
                .user(user)
                .title(task.title())
                .description(task.description())
                .createdAt(LocalDateTime.now())
                .dueDate(task.dueDate())
                .isCompleted(false)
                .build();

        return taskMapper.taskToReadTaskDto(taskRepository.save(newTask));
    }

    @CachePut(value = "tasks", key = "#taskId")
    public ReadTaskDto updateTask(Long taskId, UpdateTaskDto dto, User user) {
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId()).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        boolean wasIncomplete = !task.isCompleted();

        if (dto.title() != null) {
            task.setTitle(dto.title());
        }

        if (dto.description() != null) {
            task.setDescription(dto.description());
        }

        if (dto.dueDate() != null) {
            task.setDueDate(dto.dueDate());
        }

        if (dto.isCompleted() != null) {
            task.setCompleted(dto.isCompleted());
        }

        if (wasIncomplete && task.isCompleted()) {
            completedTasksCounter.increment();
        }

        return taskMapper.taskToReadTaskDto(taskRepository.save(task));
    }

    public List<ReadTaskDto> getUserTasksWithPagination(User user, int limit, int offset) {
        List<Task> tasks = taskRepository.findByUserIdWithPagination(user.getId(), limit, offset);
        return tasks.stream()
                .map(taskMapper::taskToReadTaskDto)
                .toList();
    }

    @CacheEvict(value = "tasks", key = "#taskId")
    public void deleteTask(Long taskId, User user) {
        Task task = taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        taskRepository.delete(task);
    }

    public List<ReadTaskDto> getAllUserTasks(User user) {
        return taskRepository.findByUserId(user.getId())
                .stream()
                .map(taskMapper::taskToReadTaskDto)
                .toList();
    }
    @Cacheable(value = "tasks", key = "#taskId", unless = "#result == null")
    public ReadTaskDto getTaskById(Long taskId, User user) {
        return taskMapper.taskToReadTaskDto(taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found")));
    }
}
//    @Scheduled(fixedRate = 3600000) // Каждый час
//    public void sendReminders() {
//        LocalDateTime now = LocalDateTime.now();
//        List<Task> tasks = taskRepository.findByDueDateBetweenAndReminderSentFalse(
//                now, now.plusDays(1));
//
//        tasks.forEach(task -> {
//            // Логика отправки напоминания (email/websocket)
//            task.setReminderSent(true);
//            taskRepository.save(task);
//        });
//    }