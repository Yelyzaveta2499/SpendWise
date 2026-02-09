package com.example.SpendWise.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.SpendWise.model.entity.RoleEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.RoleRepository;
import com.example.SpendWise.model.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Simple helper to create a user with an existing role name
    public Optional<UserEntity> createUser(String username, String rawPassword, String roleName) {
        if (username == null || username.isBlank()) return Optional.empty();
        if (rawPassword == null || rawPassword.isBlank()) return Optional.empty();
        if (roleName == null || roleName.isBlank()) return Optional.empty();

        if (userRepository.findByUsername(username).isPresent()) {
            return Optional.empty(); // username taken
        }

        Optional<RoleEntity> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            return Optional.empty(); // role not found
        }

        UserEntity user = new UserEntity();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(roleOpt.get());

        return Optional.of(userRepository.save(user));
    }
}

