package com.example.SpendWise.config;

import com.example.SpendWise.security.JwtAuthenticationFilter;
import com.example.SpendWise.security.JwtUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;

/**
 * tests for SecurityConfig.
 * verify that key beans are present and of expected types.
 */
@SpringBootTest
class SecurityConfigTest {

    private final ApplicationContext context;

    SecurityConfigTest(ApplicationContext context) {
        this.context = context;
    }

    @Test
    void securityBeans_arePresentAndCorrectType() {
        SecurityFilterChain chain = context.getBean(SecurityFilterChain.class);
        assertNotNull(chain);

        AuthenticationManager authenticationManager = context.getBean(AuthenticationManager.class);
        assertNotNull(authenticationManager);

        DaoAuthenticationProvider provider = context.getBean(DaoAuthenticationProvider.class);
        assertNotNull(provider);

        PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
        assertNotNull(encoder);
    }

    @Test
    void jwtBeans_exist() {
        JwtAuthenticationFilter filter = context.getBean(JwtAuthenticationFilter.class);
        JwtUserDetailsService userDetailsService = context.getBean(JwtUserDetailsService.class);

        assertNotNull(filter);
        assertNotNull(userDetailsService);
    }
}

