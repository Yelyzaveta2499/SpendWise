package com.example.SpendWise.security;

import com.example.SpendWise.model.entity.RoleEntity;
import com.example.SpendWise.model.entity.UserEntity;
import com.example.SpendWise.model.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * unit tests for JwtUserDetailsService.
 * Focus on: user found vs user not found.
 */
class JwtUserDetailsServiceTest {

    private UserRepository userRepository;
    private JwtUserDetailsService jwtUserDetailsService;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        jwtUserDetailsService = new JwtUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_userExists_returnsUserDetails() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("indiv");
        userEntity.setPassword("encrypted-pass");

        RoleEntity role = new RoleEntity();
        role.setName("INDIVIDUAL");
        userEntity.setRole(role);

        when(userRepository.findByUsername("indiv")).thenReturn(Optional.of(userEntity));

        UserDetails details = jwtUserDetailsService.loadUserByUsername("indiv");

        assertEquals("indiv", details.getUsername());
        assertEquals("encrypted-pass", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_INDIVIDUAL")));
    }

    @Test
    void loadUserByUsername_userMissing_throwsException() {
        when(userRepository.findByUsername("missing"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> jwtUserDetailsService.loadUserByUsername("missing"));
    }
}

