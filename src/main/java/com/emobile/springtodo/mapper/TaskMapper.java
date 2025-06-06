package com.emobile.springtodo.mapper;

import com.emobile.springtodo.dto.ReadTaskDto;
import com.emobile.springtodo.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TaskMapper {

   @Mapping(source = "user.id", target = "userId")
   @Mapping(source = "createdAt", target = "createAt")
   ReadTaskDto taskToReadTaskDto(Task task);

}
