package com.example.SpendWise.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.SpendWise.dto.UserSettingsDto;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    private void updateUserFields(UserEntity user, UserSettingsDto updated) {
        if (updated.getFirstName() != null) {
            user.setFirstName(updated.getFirstName().trim());
        }
        if (updated.getLastName() != null) {
            user.setLastName(updated.getLastName().trim());
        }
        if (updated.getEmail() != null) {
            String email = updated.getEmail().trim();
            if (email.contains("@")) {
                user.setEmail(email);
            }
        }
        if (updated.getCurrency() != null) {
            user.setCurrency(updated.getCurrency().trim());
        }
        if (updated.getAccountType() != null) {
            String accountType = updated.getAccountType().trim().toUpperCase();
            if ("BUSINESS".equals(accountType) || "PERSONAL".equals(accountType)) {
                user.setAccountType(accountType);
            }
        }
    }

    private UserSettingsDto createUserSettingsDto(UserEntity user) {
        UserSettingsDto dto = new UserSettingsDto();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setCurrency(user.getCurrency());
        dto.setAccountType(user.getAccountType());
        return dto;
    }

    /**
     * Updates settings for the given username
     */
    public Optional<UserSettingsDto> updateSettingsForUser(String username, UserSettingsDto updated) {
        return userRepository.findByUsername(username).map(user -> {
            updateUserFields(user, updated);
            UserEntity saved = userRepository.save(user);
            return createUserSettingsDto(saved);
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
