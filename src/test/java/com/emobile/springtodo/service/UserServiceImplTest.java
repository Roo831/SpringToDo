package com.emobile.springtodo.service;


import com.emobile.springtodo.dto.UpdateUserDto;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.exception.ResourceAlreadyExistsException;
import com.emobile.springtodo.exception.ResourceNotFoundException;
import com.emobile.springtodo.mapper.UserMapper;
import com.emobile.springtodo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;


    @Test
    @DisplayName("Должен обновить пользователя и вернуть ДТО")
    void updateUser_shouldUpdateFields() {
        User user = User.builder()
                .email("old@example.com")
                .createdAt()
                .updatedAt()
                .build();


        UpdateUserDto dto = new UpdateUserDto("new@example.com", "newpass");

        when(userRepository.findByEmail("old@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");

        userService.updateUser(dto, user);

        assertEquals("new@example.com", user.getUsername());
        assertEquals("encoded", user.getPassword());
        verify(userRepository).save(user);
    }


    @Test
    @DisplayName("Должен выбросить ResourceAlreadyExistsException при попытке использовать занятый email")
    void updateUser_shouldThrowIfEmailAlreadyExists() {
        User currentUser = User.builder()
                .email("current@example.com")
                .build();

        UpdateUserDto dto = new UpdateUserDto("existing@example.com", null);

        when(userRepository.findByEmail("current@example.com")).thenReturn(Optional.of(currentUser));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> {
            userService.updateUser(dto, currentUser);
        });

        verify(userRepository, never()).save(any(User.class)); // save не вызывается
    }

    @Test
    @DisplayName("Должен выбросить ResourceNotFoundException если пользователь не найден")
    void updateUser_shouldThrowIfUserNotFound() {
        User user = User.builder().email("notfound@example.com").build();
        UpdateUserDto dto = new UpdateUserDto("new@example.com", "newpass");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(dto, user);
        });

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

}
