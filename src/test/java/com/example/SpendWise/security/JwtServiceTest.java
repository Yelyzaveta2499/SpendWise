package com.example.SpendWise.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService using a hard-coded secret + short expiration.
 * injecting values via reflection so that no external config is required.
 */
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();

        // Inject secret and expiration using reflection
        setField(jwtService, "secretKey",
                // 32-byte base64 string -> HS256 compatible key
                "VGhpcy1pcy1hLXRlc3Qtand0LXNlY3JldC1rZXktMTIzNA==");
        setField(jwtService, "expirationMs", 60_000L); // 1 minute
    }

    @Test
    void generateAndValidateToken_roundTripSuccess() {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_INDIVIDUAL");
        UserDetails userDetails = new User("indiv", "password",
                Collections.singleton(authority));

        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);

        String usernameFromToken = jwtService.extractUsername(token);
        assertEquals("indiv", usernameFromToken);

        boolean valid = jwtService.isTokenValid(token, userDetails);
        assertTrue(valid);
    }

    @Test
    void isTokenValid_withDifferentUser_returnsFalse() {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_INDIVIDUAL");
        UserDetails userDetails = new User("indiv", "password",
                Collections.singleton(authority));

        String token = jwtService.generateToken(userDetails);

        UserDetails otherUser = new User("other", "password",
                Collections.singleton(authority));

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = JwtService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

