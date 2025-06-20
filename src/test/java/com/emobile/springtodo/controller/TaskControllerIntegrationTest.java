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
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.cache.type=simple"
})
@Sql(scripts = {"/sql/clear-tables.sql", "/sql/insert-user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
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

        ResultActions result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        String expectedJson = """
            {
                "title": "Title",
                "description": "Desc"
            }
        """;
        String actualJson = result.andReturn().getResponse().getContentAsString();
        assertEquals(expectedJson, actualJson, false);
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

        ResultActions result = mockMvc.perform(get("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        String expectedJson = """
            [
                {
                    "title": "Sample",
                    "description": "For Get"
                }
            ]
        """;
        String actualJson = result.andReturn().getResponse().getContentAsString();
        assertEquals(expectedJson, actualJson, false);
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
        ResultActions result1 = mockMvc.perform(get("/api/tasks/paged")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("limit", "2")
                        .param("offset", "0"))
                .andExpect(status().isOk());

        String expectedJson1 = """
            [
                { "title": "Task 1" },
                { "title": "Task 2" }
            ]
        """;
        String actualJson1 = result1.andReturn().getResponse().getContentAsString();
        assertEquals(expectedJson1, actualJson1, false);

        // вторая страница
        ResultActions result2 = mockMvc.perform(get("/api/tasks/paged")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("limit", "2")
                        .param("offset", "2"))
                .andExpect(status().isOk());

        String expectedJson2 = """
            [
                { "title": "Task 3" }
            ]
        """;
        String actualJson2 = result2.andReturn().getResponse().getContentAsString();
        assertEquals(expectedJson2, actualJson2, false);
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

        ResultActions result = mockMvc.perform(put("/api/tasks/" + task.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        String expectedJson = """
            {
                "title": "New",
                "completed": true
            }
        """;
        String actualJson = result.andReturn().getResponse().getContentAsString();
        assertEquals(expectedJson, actualJson, false);
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

    @Test
    @DisplayName("Должен вернуть 404, если задача не найдена")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        Long nonExistentTaskId = 9999L;

        ResultActions result = mockMvc.perform(get("/api/tasks/" + nonExistentTaskId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());

        String expectedJson = """
            {
                "message": "Resource not found"
            }
        """;
        String actualJson = result.andReturn().getResponse().getContentAsString();
        assertEquals(expectedJson, actualJson, false);
    }

    @Test
    @DisplayName("Должен вернуть 400 при валидационной ошибке")
    void shouldReturn400ForValidationError() throws Exception {
        CreateTaskDto createTaskDto = new CreateTaskDto(null, "Desc", LocalDateTime.now().plusDays(1));

        ResultActions result = mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTaskDto)))
                .andExpect(status().isBadRequest());

        String expectedJson = """
            {
                "title": "must not be blank"
            }
        """;
        String actualJson = result.andReturn().getResponse().getContentAsString();
        assertEquals(expectedJson, actualJson, false);
    }
}