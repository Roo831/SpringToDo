package com.emobile.springtodo.controller;


import com.emobile.springtodo.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.emobile.springtodo.AbstractPostgresContainer;
import com.emobile.springtodo.dto.CreateTaskDto;
import com.emobile.springtodo.dto.UpdateTaskDto;
import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.repository.TaskRepository;
import com.emobile.springtodo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Sql(scripts = {"/sql/clear-tables.sql", "/sql/insert-user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.cache.type=simple"
})
public class TaskControllerIntegrationTest extends AbstractPostgresContainer {

    @Autowired private MockMvc mockMvc;
    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    private String jwtToken;

    private static final String EMAIL = "user@example.com";

    @BeforeEach
    void setup() throws Exception {

        String json = """
                {
                  "email": "user@example.com",
                  "password": "encoded_password"
                }
                """;

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        jwtToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @DisplayName("Должен создать задачу")
    void testCreateTask() throws Exception {
        CreateTaskDto dto = new CreateTaskDto("Title", "Desc", LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.description").value("Desc"));
    }

    @Test
    @DisplayName("Должен вернуть список задач")
    void testGetTasks() throws Exception {
        User testUser = userRepository.findByEmail(EMAIL).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        Task task = Task.builder()
                .title("Sample")
                .description("For Get")
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .user(testUser)
                .build();
        taskRepository.save(task);

        mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Sample"));
    }

    @Test
    @DisplayName("Должен вернуть задачи постранично")
    void testGetTasksPaged() throws Exception {
        User testUser = userRepository.findByEmail(EMAIL)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        // Добавим 3 задачи
        for (int i = 1; i <= 3; i++) {
            Task task = Task.builder()
                    .title("Task " + i)
                    .description("Description " + i)
                    .createdAt(LocalDateTime.now().minusDays(i))
                    .dueDate(LocalDateTime.now().plusDays(i))
                    .user(testUser)
                    .build();
            taskRepository.save(task);
        }

        // первая страница (limit=2, offset=0)
        mockMvc.perform(get("/api/tasks/paged")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("limit", "2")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"));

        // вторая страница (limit=2, offset=2)
        mockMvc.perform(get("/api/tasks/paged")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("limit", "2")
                        .param("offset", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Task 3"));
    }

    @Test
    @DisplayName("Должен обновить задачу")
    void testUpdateTask() throws Exception {
        User testUser = userRepository.findByEmail(EMAIL).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        Task task = Task.builder()
                .title("Old")
                .description("Old Desc")
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .user(testUser)
                .build();
        task = taskRepository.save(task);

        UpdateTaskDto dto = new UpdateTaskDto("New", "New Desc", LocalDateTime.now().plusDays(2), true);

        mockMvc.perform(put("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    @DisplayName("Должен удалить задачу")
    void testDeleteTask() throws Exception {
        User testUser = userRepository.findByEmail(EMAIL).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        Task task = Task.builder()
                .title("To Delete")
                .description("...")
                .createdAt(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(1))
                .user(testUser)
                .build();
        task = taskRepository.save(task);

        mockMvc.perform(delete("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());
    }

    @DisplayName("Должен вернуть 404, если задача не найдена")
    @Test
    void shouldReturn404WhenTaskNotFound() throws Exception {
        Long nonExistentTaskId = 9999L;

        mockMvc.perform(get("/api/tasks/" + nonExistentTaskId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Resource not found"));
    }

    @DisplayName("Должен вернуть 400 при валидационной ошибке")
    @Test
    void shouldReturn400ForValidationError() throws Exception {

        CreateTaskDto createTaskDto = new CreateTaskDto(null, "Desc", LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }
}
