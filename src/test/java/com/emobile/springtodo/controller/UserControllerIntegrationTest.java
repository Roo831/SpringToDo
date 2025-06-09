package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.AuthRequest;
import com.emobile.springtodo.dto.UpdateUserDto;
import com.emobile.springtodo.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.emobile.springtodo.AbstractPostgresContainer;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
        mvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("user@example.com")))
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())));
    }

    @Test
    @DisplayName("Должен обновить email и пароль пользователя")
    void shouldUpdateUserEmailAndPassword() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto("newemail@example.com", "newpassword");
        User testUser = userRepository.findByEmail(EMAIL).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        mvc.perform(put("/api/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("newemail@example.com")));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getEmail().equals("newemail@example.com");
    }

    @Test
    @DisplayName("Должен обновить только email")
    void shouldUpdateOnlyEmail() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto("updated@example.com", null);
        User testUser = userRepository.findByEmail(EMAIL).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        mvc.perform(put("/api/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("updated@example.com")));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getEmail().equals("updated@example.com");
    }

    @Test
    @DisplayName("Должен обновить только пароль")
    void shouldUpdateOnlyPassword() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto(null, "updatedPassword");
        User testUser = userRepository.findByEmail(EMAIL).orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
        mvc.perform(put("/api/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(testUser.getEmail())));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert !updatedUser.getPassword().equals("encoded-password");
    }

    @Test
    @DisplayName("Должен вернуть 400, если email невалидный")
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto("invalid-email", "newpass");

        mvc.perform(put("/api/users")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Должен вернуть 403, если пользователь не аутентифицирован")
    void shouldReturnUnauthorizedIfNotAuthenticated() throws Exception {
            mvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("Должен вернуть 409, если пользователь уже существует")
    @Test
    void shouldReturn409WhenUserAlreadyExists() throws Exception {

        AuthRequest authRequest = new AuthRequest(EMAIL, "some_password");

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already in use"));
    }

    @DisplayName("Должен вернуть 400 при неправильных логин/пароле")
    @Test
    void shouldReturn400ForBadCredentials() throws Exception {

        AuthRequest request = new AuthRequest("wrong@example.com", "invalid");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
