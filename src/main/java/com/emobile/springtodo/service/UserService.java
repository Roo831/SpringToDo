package com.emobile.springtodo.service;


import com.emobile.springtodo.dto.ReadUserDto;
import com.emobile.springtodo.dto.RegisterDto;
import com.emobile.springtodo.dto.UpdateUserDto;
import com.emobile.springtodo.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;



public interface UserService extends UserDetailsService {
    User createUser(RegisterDto request);

    ReadUserDto getByEmail(String username);

    ReadUserDto updateUser(UpdateUserDto updateUserDto, User user);
}