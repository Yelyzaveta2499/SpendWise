package com.example.SpendWise.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.SpendWise.dto.UserSettingsDto;
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

    // helper to create a user with an existing role name
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

    // --- Settings section ---
    public Optional<UserSettingsDto> getSettingsForUser(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    UserSettingsDto dto = new UserSettingsDto();
                    dto.setFirstName(user.getFirstName());
                    dto.setLastName(user.getLastName());
                    dto.setEmail(user.getEmail());
                    dto.setCurrency(user.getCurrency());
                    dto.setAccountType(user.getAccountType());
                    return dto;
                });
    }

    /**
     * Updates settings for the given username
     */
    public Optional<UserSettingsDto> updateSettingsForUser(String username, UserSettingsDto updated) {
        return userRepository.findByUsername(username).map(user -> {
            String firstName = updated.getFirstName();
            String lastName  = updated.getLastName();
            String email     = updated.getEmail();
            String currency  = updated.getCurrency();
            String accountType = updated.getAccountType();

            if (firstName != null) {
                user.setFirstName(firstName.trim());
            }
            if (lastName != null) {
                user.setLastName(lastName.trim());
            }

            if (email != null) {
                email = email.trim();
                if (!email.isEmpty() && email.contains("@")) {
                    user.setEmail(email);
                }
            }

            if (currency != null) {
                user.setCurrency(currency.trim());
            }

            if (accountType != null) {
                String at = accountType.trim().toUpperCase();
                if ("BUSINESS".equals(at) || "PERSONAL".equals(at)) {
                    user.setAccountType(at);
                }
            }

            UserEntity saved = userRepository.save(user);

            UserSettingsDto result = new UserSettingsDto();
            result.setFirstName(saved.getFirstName());
            result.setLastName(saved.getLastName());
            result.setEmail(saved.getEmail());
            result.setCurrency(saved.getCurrency());
            result.setAccountType(saved.getAccountType());
            return result;
        });
    }

    /**
     * Soft-deletes the user account for the given username by setting
     * a  boolean flag. Data stays in the database.
     */
    public boolean deleteAccountForUser(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    user.setDeleted(true);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }
}

