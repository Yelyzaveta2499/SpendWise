package com.example.SpendWise.unit.service;

import com.example.SpendWise.dto.UserSettingsDto;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.RoleRepository;
import com.example.SpendWise.model.repository.UserRepository;
import com.example.SpendWise.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository userRepository;


    private UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);


        userService = new UserService(userRepository);
    }

    @Test
    void getSettingsForUser_returnsDtoWhenUserExists() {
        UserEntity entity = new UserEntity();
        entity.setUsername("liza");
        entity.setFirstName("Liza");
        entity.setLastName("Hlushych");
        entity.setEmail("liza@example.com");
        entity.setCurrency("usd");
        entity.setAccountType("BUSINESS");

        when(userRepository.findByUsername("liza")).thenReturn(Optional.of(entity));

        Optional<UserSettingsDto> result = userService.getSettingsForUser("liza");

        assertTrue(result.isPresent());
        UserSettingsDto dto = result.get();
        assertEquals("Liza", dto.getFirstName());
        assertEquals("Hlushych", dto.getLastName());
        assertEquals("liza@example.com", dto.getEmail());
        assertEquals("usd", dto.getCurrency());
        assertEquals("BUSINESS", dto.getAccountType());
    }

    @Test
    void updateSettingsForUser_updatesAndReturnsDto() {
        UserEntity entity = new UserEntity();
        entity.setUsername("liza");
        entity.setFirstName("Old");
        entity.setLastName("Name");
        entity.setEmail("old@example.com");
        entity.setCurrency("eur");
        entity.setAccountType("PERSONAL");

        when(userRepository.findByUsername("liza")).thenReturn(Optional.of(entity));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserSettingsDto incoming = new UserSettingsDto();
        incoming.setFirstName("Liza");
        incoming.setLastName("Hlushych");
        incoming.setEmail("liza@example.com");
        incoming.setCurrency("usd");
        incoming.setAccountType("BUSINESS");

        Optional<UserSettingsDto> updatedOpt = userService.updateSettingsForUser("liza", incoming);

        assertTrue(updatedOpt.isPresent());
        UserSettingsDto updated = updatedOpt.get();
        assertEquals("Liza", updated.getFirstName());
        assertEquals("Hlushych", updated.getLastName());
        assertEquals("liza@example.com", updated.getEmail());
        assertEquals("usd", updated.getCurrency());
        assertEquals("BUSINESS", updated.getAccountType());

        //  verify the entity was actually changed
        assertEquals("Liza", entity.getFirstName());
        assertEquals("Hlushych", entity.getLastName());
        assertEquals("liza@example.com", entity.getEmail());
        assertEquals("usd", entity.getCurrency());
        assertEquals("BUSINESS", entity.getAccountType());
    }

    @Test
    void deleteAccountForUser_marksUserAsDeleted() {
        UserEntity entity = new UserEntity();
        entity.setUsername("liza");
        entity.setDeleted(false);

        when(userRepository.findByUsername("liza")).thenReturn(Optional.of(entity));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = userService.deleteAccountForUser("liza");

        assertTrue(result);
        assertTrue(entity.isDeleted());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertTrue(captor.getValue().isDeleted());
    }
}

