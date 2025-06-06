package com.emobile.springtodo.mapper;


import com.emobile.springtodo.dto.ReadUserDto;
import com.emobile.springtodo.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {


    @Mapping(source = "email", target = "email")
    @Mapping(source = "createdAt", target = "createdAt")
    ReadUserDto userToReadUserDto(User user);
}