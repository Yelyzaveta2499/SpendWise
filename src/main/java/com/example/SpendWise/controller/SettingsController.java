package com.example.SpendWise.controller;

import com.example.SpendWise.dto.UserSettingsDto;
import com.example.SpendWise.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;


@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final UserService userService;

    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserSettingsDto> getSettings(Authentication authentication) {
        String username = authentication.getName();
        Optional<UserSettingsDto> dtoOpt = userService.getSettingsForUser(username);

        return dtoOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping
    public ResponseEntity<UserSettingsDto> updateSettings(@RequestBody UserSettingsDto body,
                                                          Authentication authentication) {
        String username = authentication.getName();

        Optional<UserSettingsDto> updated = userService.updateSettingsForUser(username, body);
        return updated
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}

