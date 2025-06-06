package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.UpdateUserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.emobile.springtodo.AbstractPostgresContainer;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest extends AbstractPostgresContainer {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
//        userRepository.deleteAll(); Заменить на JpaTest аннотацию

        testUser = User.builder()
                .email("test@example.com")
                .password("encoded-password")
                .createdAt()
                .updatedAt()
                .build();

        testUser = userRepository.save(testUser);

        var auth = new TestingAuthenticationToken(testUser, testUser.getPassword(), "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    void shouldGetCurrentUser() throws Exception {
        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.id", is(testUser.getId().intValue())));
    }

    @Test
    void shouldUpdateUserEmailAndPassword() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto("newemail@example.com", "newpassword");

        mvc.perform(put("/api/users")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("newemail@example.com")));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getEmail().equals("newemail@example.com");
    }

    @Test
    void shouldUpdateOnlyEmail() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto("updated@example.com", null);

        mvc.perform(put("/api/users")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("updated@example.com")));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert updatedUser.getEmail().equals("updated@example.com");
    }

    @Test
    void shouldUpdateOnlyPassword() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto(null, "updatedPassword");

        mvc.perform(put("/api/users")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(testUser.getEmail()))); // email не должен измениться

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assert !updatedUser.getPassword().equals("encoded-password"); // пароль обновлён (если шифруется)
    }

    @Test
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        UpdateUserDto updateDto = new UpdateUserDto("invalid-email", "newpass");

        mvc.perform(put("/api/users")
                        .with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedIfNotAuthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        mvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

}
