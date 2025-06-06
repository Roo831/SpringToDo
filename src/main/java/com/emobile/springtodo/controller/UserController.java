package com.emobile.springtodo.controller;


import com.emobile.springtodo.dto.ReadUserDto;
import com.emobile.springtodo.dto.UpdateUserDto;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<ReadUserDto> getUser(@AuthenticationPrincipal User user) {
        ReadUserDto userFromDB = userService.getByEmail(user.getUsername());
        return userFromDB != null ? ResponseEntity.ok(userFromDB) : ResponseEntity.notFound().build();
    }

    @PutMapping
    public ResponseEntity<ReadUserDto> updateUser(@AuthenticationPrincipal User user, @RequestBody @Valid UpdateUserDto updateUserDto) {
        return ResponseEntity.ok(userService.updateUser(updateUserDto, user));
    }
}