--liquibase formatted sql
--changeset rpoptsov:1

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL
);

CREATE TABLE tasks (
                       id BIGSERIAL PRIMARY KEY,
                       user_id BIGINT NOT NULL REFERENCES users(id),
                       title VARCHAR(255) NOT NULL,
                       description TEXT,
                       created_at TIMESTAMP NOT NULL,
                       due_date TIMESTAMP,
                       is_completed BOOLEAN NOT NULL DEFAULT false,
                       reminder_sent BOOLEAN DEFAULT false
);

CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_task_user ON tasks(user_id);
