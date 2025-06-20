package com.emobile.springtodo.controller;


import com.emobile.springtodo.dto.ReadUserDto;
import com.emobile.springtodo.dto.UpdateUserDto;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.service.UserService;
import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Управление данными пользователя")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Timed(value = "users.get.time", description = "Время получения данных пользователя")
    @Counted(value = "users.get.count", description = "Количество запросов на получение данных пользователя")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Получить текущего пользователя")
    public ReadUserDto getUser(@AuthenticationPrincipal User user) {
        return userService.getByEmail(user.getUsername());
    }

    @Timed(value = "users.update.time", description = "Время обновления данных пользователя")
    @Counted(value = "users.update.count", description = "Количество обновлений пользователя")
    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Обновить данные пользователя")
    public ReadUserDto updateUser(@AuthenticationPrincipal User user, @RequestBody @Valid UpdateUserDto updateUserDto) {
        return userService.updateUser(updateUserDto, user);
    }
}