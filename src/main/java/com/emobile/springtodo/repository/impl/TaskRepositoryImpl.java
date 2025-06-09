package com.emobile.springtodo.repository.impl;

import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.mapper.TaskRowMapper;
import com.emobile.springtodo.repository.TaskRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class TaskRepositoryImpl implements TaskRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TaskRowMapper taskRowMapper = new TaskRowMapper();

    public TaskRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Task> findByUserId(Long userId) {
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        return jdbcTemplate.query(sql, taskRowMapper, userId);
    }

    @Override
    public List<Task> findByUserIdWithPagination(Long userId, int limit, int offset) {
        String sql = "SELECT * FROM tasks WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, taskRowMapper, userId, limit, offset);
    }

    @Override
    public Optional<Task> findByIdAndUserId(Long id, Long userId) {
        String sql = "SELECT * FROM tasks WHERE id = ? AND user_id = ?";
        return jdbcTemplate.query(sql, taskRowMapper, id, userId)
                .stream()
                .findFirst();
    }

    @Override
    public Task save(Task task) {
        if (task.getId() == null) {
            String sql = """
                    INSERT INTO tasks (user_id, title, description, created_at, due_date, is_completed)
                    VALUES (?, ?, ?, ?, ?, ?)
                    RETURNING id
                    """;
            Long id = jdbcTemplate.queryForObject(sql, Long.class,
                    task.getUser().getId(),
                    task.getTitle(),
                    task.getDescription(),
                    Timestamp.valueOf(task.getCreatedAt()),
                    task.getDueDate() != null ? Timestamp.valueOf(task.getDueDate()) : null,
                    task.isCompleted());
            task.setId(id);
        } else {
            String sql = """
                    UPDATE tasks SET title = ?, description = ?, due_date = ?, is_completed = ?
                    WHERE id = ? AND user_id = ?
                    """;
            jdbcTemplate.update(sql,
                    task.getTitle(),
                    task.getDescription(),
                    task.getDueDate() != null ? Timestamp.valueOf(task.getDueDate()) : null,
                    task.isCompleted(),
                    task.getId(),
                    task.getUser().getId());
        }
        return task;
    }

    @Override
    public void delete(Task task) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        jdbcTemplate.update(sql, task.getId());
    }
}
