package com.emobile.springtodo.mapper;

import com.emobile.springtodo.entity.Task;
import com.emobile.springtodo.entity.User;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class TaskRowMapper implements RowMapper<Task> {
    @Override
    public Task mapRow(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));

        return Task.builder()
                .id(rs.getLong("id"))
                .user(user)
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .dueDate(rs.getTimestamp("due_date") != null ? rs.getTimestamp("due_date").toLocalDateTime() : null)
                .isCompleted(rs.getBoolean("is_completed"))
                .build();
    }
}