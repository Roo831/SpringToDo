package com.emobile.springtodo.repository.impl;

import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.mapper.UserRowMapper;
import com.emobile.springtodo.repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper = new UserRowMapper();

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        return jdbcTemplate.query(sql, userRowMapper, id)
                .stream()
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbcTemplate.query(sql, userRowMapper, email)
                .stream()
                .findFirst();
    }

    @Override
    public Boolean existsByEmail(String email) {
        String sql = "SELECT count(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public User save(User user) {
        if (user.getId() == null) {
            String sql = """
                    INSERT INTO users (email, password, created_at, updated_at)
                    VALUES (?, ?, ?, ?)
                    RETURNING id
                    """;
            Long id = jdbcTemplate.queryForObject(sql, Long.class,
                    user.getEmail(),
                    user.getPassword(),
                    Timestamp.valueOf(user.getCreatedAt()),
                    Timestamp.valueOf(user.getUpdatedAt()));
            user.setId(id);
        } else {
            String sql = """
                    UPDATE users SET email = ?, password = ?, updated_at = ?
                    WHERE id = ?
                    """;
            jdbcTemplate.update(sql,
                    user.getEmail(),
                    user.getPassword(),
                    Timestamp.valueOf(user.getUpdatedAt()),
                    user.getId());
        }
        return user;
    }
}
