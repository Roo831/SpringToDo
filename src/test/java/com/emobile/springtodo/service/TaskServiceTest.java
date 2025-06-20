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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
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
    @Mock private MeterRegistry meterRegistry;
    @Mock private Counter counter;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        when(meterRegistry.counter("tasks.completed.count")).thenReturn(counter);
        taskService = new TaskService(taskRepository, taskMapper, meterRegistry);
    }

    @Test
    @DisplayName("Должен создать и вернуть таску")
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
    @DisplayName("Должен успешно вернуть таску по ID")
    void getTaskById_shouldReturnTaskDto() {
        User user = new User();
        user.setId(1L);

        Task task = Task.builder()
                .id(1L)
                .user(user)
                .title("Test Task")
                .description("Description")
                .dueDate(LocalDateTime.now().plusDays(1))
                .isCompleted(false)
                .build();

        ReadTaskDto dto = new ReadTaskDto(
                1L, 1L,
                "Test Task", "Description",
                LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                false);

        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(task));
        when(taskMapper.taskToReadTaskDto(task)).thenReturn(dto);

        ReadTaskDto result = taskService.getTaskById(1L, user);

        assertEquals("Test Task", result.title());
        verify(taskRepository).findByIdAndUserId(1L, 1L);
    }

    @Test
    @DisplayName("Должен обновить задачу и увеличить счётчик завершённых задач")
    void updateTask_shouldUpdateTitleAndIncrementCounter() throws Exception {
        User user = new User();
        user.setId(1L);

        Task existingTask = Task.builder()
                .id(1L)
                .user(user)
                .title("Old Title")
                .description("Old Description")
                .dueDate(LocalDateTime.now().plusDays(2))
                .isCompleted(false)
                .build();

        UpdateTaskDto dto = new UpdateTaskDto("New Title", null, null, true);

        Task updatedTask = Task.builder()
                .id(1L)
                .user(user)
                .title("New Title")
                .description("Old Description")
                .dueDate(existingTask.getDueDate())
                .isCompleted(true)
                .build();

        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);
        when(taskMapper.taskToReadTaskDto(updatedTask)).thenReturn(
                new ReadTaskDto(
                        1L, 1L,
                        "New Title", "Old Description",
                        LocalDateTime.now(), existingTask.getDueDate(),
                        true));

        ReadTaskDto result = taskService.updateTask(1L, dto, user);

        assertEquals("New Title", result.title());
        assertEquals(true, result.completed());
        verify(counter).increment();
        verify(taskRepository).save(updatedTask);
    }

    @Test
    @DisplayName("Должен вызвать delete один раз при удалении задачи")
    void deleteTask_shouldCallDeleteOnce() {
        User user = new User();
        user.setId(1L);

        Task task = Task.builder()
                .id(1L)
                .user(user)
                .title("ToDelete")
                .description("Descr")
                .dueDate(LocalDateTime.now().plusDays(1))
                .isCompleted(false)
                .build();

        when(taskRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L, user);

        verify(taskRepository).delete(task);
    }

    @Test
    @DisplayName("Должен вернуть список задач пользователя")
    void getAllUserTasks_shouldReturnListOfDtos() {
        User user = new User();
        user.setId(1L);

        Task task1 = Task.builder()
                .id(1L)
                .user(user)
                .title("Task 1")
                .description("Descr 1")
                .dueDate(LocalDateTime.now().plusDays(1))
                .isCompleted(false)
                .build();

        Task task2 = Task.builder()
                .id(2L)
                .user(user)
                .title("Task 2")
                .description("Descr 2")
                .dueDate(LocalDateTime.now().plusDays(2))
                .isCompleted(true)
                .build();

        List<Task> tasks = List.of(task1, task2);

        when(taskRepository.findByUserId(1L)).thenReturn(tasks);
        when(taskMapper.taskToReadTaskDto(task1)).thenReturn(new ReadTaskDto(
                1L, 1L, "Task 1", "Descr 1", LocalDateTime.now(), LocalDateTime.now().plusDays(1), false));
        when(taskMapper.taskToReadTaskDto(task2)).thenReturn(new ReadTaskDto(
                2L, 1L, "Task 2", "Descr 2", LocalDateTime.now(), LocalDateTime.now().plusDays(2), true));

        List<ReadTaskDto> result = taskService.getAllUserTasks(user);

        assertEquals(2, result.size());
        assertEquals("Task 1", result.get(0).title());
        assertEquals("Task 2", result.get(1).title());
    }

    @Test
    @DisplayName("Должен выбросить исключение, если таска не найдена")
    void getTaskById_shouldThrowIfNotFound() {
        User user = new User();
        user.setId(1L);

        when(taskRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.getTaskById(99L, user));
    }
}