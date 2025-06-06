package com.emobile.springtodo.service;


import com.emobile.springtodo.dto.AuthRequest;
import com.emobile.springtodo.dto.AuthResponse;
import com.emobile.springtodo.dto.RegisterDto;
import com.emobile.springtodo.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserServiceImpl userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    public AuthService(UserServiceImpl userService, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthResponse register(RegisterDto request) {
        log.info("Registering new user with email: {}", request.email());
        User user = userService.createUser(request);
        var token = jwtService.generateToken(user);
        log.debug("User registered successfully: {}", user.getUsername());
        return new AuthResponse(token);
    }

    public AuthResponse authenticate(AuthRequest request) {
        log.info("Authenticating user: {}", request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );
        var user = userService.loadUserByUsername(request.email());
        var token = jwtService.generateToken(user);
        log.debug("User authenticated successfully: {}", user.getUsername());
        return new AuthResponse(token);
    }
}