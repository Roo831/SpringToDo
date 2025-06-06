package com.emobile.springtodo.service;


import com.emobile.springtodo.dto.ReadUserDto;
import com.emobile.springtodo.dto.RegisterDto;
import com.emobile.springtodo.dto.UpdateUserDto;
import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.exception.ResourceAlreadyExistsException;
import com.emobile.springtodo.exception.ResourceNotFoundException;
import com.emobile.springtodo.mapper.UserMapper;
import com.emobile.springtodo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@Primary
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", email);
                    return new UsernameNotFoundException("User not found");
                });
    }

    public User createUser(RegisterDto request) {
        log.info("Creating new user: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Email already in use: {}", request.email());
            throw new ResourceAlreadyExistsException("Email already in use");
        }

        var user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();
        return userRepository.save(user);
    }

    @Override
    public ReadUserDto getByEmail(String username) {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        System.out.println("User from DB: " + user.getUsername() + ", " + user.getCreatedAt());
        return userMapper.userToReadUserDto(user);
    }

    @Override
    public ReadUserDto updateUser(UpdateUserDto updateUserDto, User user) {
        User userFromDB = userRepository.findByEmail(user.getUsername()).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (updateUserDto.email() != null) {
            if(userRepository.existsByEmail(updateUserDto.email())){
                log.warn("Email already in use: {}", updateUserDto.email());
                throw new ResourceAlreadyExistsException("Email already in use");
            }
            userFromDB.setUsername(updateUserDto.email());
        }
       if (updateUserDto.password() != null) {
           userFromDB.setPassword(passwordEncoder.encode(updateUserDto.password()));
       }

        return userMapper.userToReadUserDto(userRepository.save(userFromDB));
    }
}