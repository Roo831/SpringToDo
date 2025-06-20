package com.emobile.springtodo.entity;



import java.time.LocalDateTime;


public class Task {
    private Long id;

    private User user;

    private String title;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime dueDate;

    private boolean isCompleted = false;

    public Task(Long id, User user, String title, String description,
                LocalDateTime createdAt, LocalDateTime dueDate, boolean isCompleted) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
    }

    public Task() {
    }

    public static TaskBuilder builder() {
        return new TaskBuilder();
    }

    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task task)) return false;
        return id != null && id.equals(task.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public Long getId() {
        return this.id;
    }

    public User getUser() {
        return this.user;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getDueDate() {
        return this.dueDate;
    }

    public boolean isCompleted() {
        return this.isCompleted;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public void setCompleted(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }

    public static class TaskBuilder {
        private Long id;
        private User user;
        private String title;
        private String description;
        private LocalDateTime createdAt;
        private LocalDateTime dueDate;
        private boolean isCompleted;

        TaskBuilder() {
        }

        public TaskBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TaskBuilder user(User user) {
            this.user = user;
            return this;
        }

        public TaskBuilder title(String title) {
            this.title = title;
            return this;
        }

        public TaskBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TaskBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TaskBuilder dueDate(LocalDateTime dueDate) {
            this.dueDate = dueDate;
            return this;
        }

        public TaskBuilder isCompleted(boolean isCompleted) {
            this.isCompleted = isCompleted;
            return this;
        }

        public Task build() {
            return new Task(this.id, this.user, this.title, this.description, this.createdAt, this.dueDate, this.isCompleted);
        }

        public String toString() {
            return "Task.TaskBuilder(id=" + this.id + ", user=" + this.user + ", title=" + this.title + ", description=" + this.description + ", createdAt=" + this.createdAt + ", dueDate=" + this.dueDate + ", isCompleted=" + this.isCompleted + ")";
        }
    }
}
