package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.Task;

import java.util.List;
import java.util.Optional;

public interface TaskRepository {
    List<Task> findByUserId(Long userId);
    List<Task> findByUserIdWithPagination(Long userId, int limit, int offset);
    Optional<Task> findByIdAndUserId(Long id, Long userId);
    Task save(Task task);
    void delete(Task task);
}