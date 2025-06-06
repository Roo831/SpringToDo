package com.emobile.springtodo.service;

import com.emobile.springtodo.dto.CreateTaskDto;
import com.emobile.springtodo.dto.ReadTaskDto;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.exception.ResourceNotFoundException;
import com.emobile.springtodo.mapper.TaskMapper;
import com.emobile.springtodo.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createTask_shouldReturnDto() {
        User user = new User();
        user.setId(1L);
        CreateTaskDto dto = new CreateTaskDto("Title", "Description", LocalDateTime.now());
        Task task = Task.builder()
                .user(user)
                .title("Title")
                .description("Description")
                .dueDate(dto.dueDate())
                .isCompleted(false)
                .build();

        when(taskRepository.save(any(Task.class))).thenReturn(task);
        when(taskMapper.taskToReadTaskDto(task)).thenReturn(
                new ReadTaskDto(1L, 1L, "Title", "Description", LocalDateTime.now(), dto.dueDate(), false));

        ReadTaskDto result = taskService.createTask(dto, user);

        assertEquals("Title", result.title());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void getTaskById_shouldThrowIfNotFound() {
        User user = new User();
        user.setId(1L);

        when(taskRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.getTaskById(99L, user));
    }
}
