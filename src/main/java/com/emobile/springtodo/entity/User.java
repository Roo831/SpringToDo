package com.emobile.springtodo.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;



public class User implements UserDetails {

    private Long id;


    private String email;


    private String password;


    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


    private List<Task> tasks;

    public User(Long id, String email, String password) {
        this.id = id;
        this.email = email;
        this.password = password;
    }

    public User() {
    }

    public User(Long id, String email, String password, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getUsername() {
        return email;
    }

    public void setUsername(String username) {
        this.email = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getEmail() {
        return this.email;
    }


    public static class UserBuilder {
        private Long id;
        private String email;
        private String password;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        UserBuilder() {
        }

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder createdAt() {
            this.createdAt = LocalDateTime.now();
            return this;
        }

        public UserBuilder updatedAt() {
            this.updatedAt = LocalDateTime.now();
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public User build() {
            return new User(this.id, this.email, this.password, this.createdAt, this.updatedAt);
        }

        public String toString() {
            return "User.UserBuilder(id=" + this.id + ", email=" + this.email + ", password=" + this.password + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ")";
        }
    }
}
