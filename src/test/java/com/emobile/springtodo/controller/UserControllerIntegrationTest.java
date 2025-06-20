package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.AuthRequest;
import com.emobile.springtodo.dto.UpdateUserDto;
import com.emobile.springtodo.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.emobile.springtodo.AbstractPostgresContainer;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

@Sql(scripts = {"/sql/clear-tables.sql", "/sql/insert-user.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
public class UserControllerIntegrationTest extends AbstractPostgresContainer {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    private static final String EMAIL = "user@example.com";

    @BeforeEach
    void setUp() throws Exception {
        String json = """
                {
                  "email": "user@example.com",
                  "password": "encoded_password"
                }
                """;

        String response = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        jwtToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @DisplayName("Должен вернуть текущего пользователя")
    void shouldGetCurrentUser() throws Exception {
        User testUser = userRepository.findByEmail(EMAIL).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));

        ResultActions result = mvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        String expectedJson = String.format("""
                    {
                        "id": %d,
                        "email": "user@example.com"
                    }
                """, testUser.getId());

        String actualJson = result.andReturn().getResponse().getContentAsString();
        assertEquals(expectedJson, actualJson, false);
    }

    @Test
    @DisplayName("Должен обновить email и пароль пользователя")
    void shouldUpdateUserEmailAndPassword() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto("newemail@example.com", "newpassword");

        ResultActions result = mvc.perform(put("/api/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        String expectedJson = """
                    {
                        "email": "newemail@example.com"
                    }
                """;

        String actualJson = result.andReturn().getResponse().getContentAsString();
        assertEquals(expectedJson, actualJson, false);

        User updatedUser = userRepository.findByEmail("newemail@example.com").orElseThrow();
        assert updatedUser.getEmail().equals("newemail@example.com");
    }

    @Test
    @DisplayName("Должен обновить только email")
    void shouldUpdateOnlyEmail() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto("updated@example.com", null);

        ResultActions result = mvc.perform(put("/api/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        String expectedJson = """
                    {
                        "email": "updated@example.com"
                    }
                """; // или можно добавить id, если нужно больше деталей

        String actualJson = result.andReturn().getResponse().getContentAsString();
        assertEquals(expectedJson, actualJson, false);

        User updatedUser = userRepository.findByEmail("updated@example.com").orElseThrow();
        assert updatedUser.getEmail().equals("updated@example.com");
    }

    @Test
    @DisplayName("Должен обновить только пароль")
    void shouldUpdateOnlyPassword() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto(null, "updatedPassword");

        ResultActions result = mvc.perform(put("/api/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        User testUser = userRepository.findByEmail(EMAIL).orElseThrow();
        String expectedJson = String.format("""
                    {
                        "email": "%s"
                    }
                """, testUser.getEmail());

        String actualJson = result.andReturn().getResponse().getContentAsString();
        assertEquals(expectedJson, actualJson, false);

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert !updatedUser.getPassword().equals("encoded-password");
    }

    @Test
    @DisplayName("Должен вернуть 400, если email невалидный")
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto("invalid-email", "newpass");

        mvc.perform(put("/api/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Должен вернуть 403, если пользователь не аутентифицирован")
    void shouldReturnUnauthorizedIfNotAuthenticated() throws Exception {
        mvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Должен вернуть 409, если пользователь уже существует")
    void shouldReturn409WhenUserAlreadyExists() throws Exception {
        AuthRequest authRequest = new AuthRequest(EMAIL, "some_password");

        ResultActions result = mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isConflict());

        String expectedJson = """
                    {
                        "message": "Email already in use"
                    }
                """;

        String actualJson = result.andReturn().getResponse().getContentAsString();
        assertEquals(expectedJson, actualJson, false);
    }

    @Test
    @DisplayName("Должен вернуть 400 при неправильных логин/пароле")
    void shouldReturn400ForBadCredentials() throws Exception {
        AuthRequest request = new AuthRequest("wrong@example.com", "invalid");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}