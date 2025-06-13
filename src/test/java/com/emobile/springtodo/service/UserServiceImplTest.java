package com.emobile.springtodo.service;


import com.emobile.springtodo.dto.UpdateUserDto;
import com.emobile.springtodo.entity.User;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
}
