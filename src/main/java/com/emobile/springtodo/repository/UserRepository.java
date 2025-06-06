package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    User save(User user);
}