package com.example.SpendWise.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    // BCrypt of "password"
    private static final String ENCODED_PASSWORD = new BCryptPasswordEncoder().encode("password");

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return switch (username) {
            case "indiv" -> User.builder()
                    .username("indiv")
                    .password(ENCODED_PASSWORD)
                    .roles("INDIVIDUAL")
                    .build();

            case "business" -> User.builder()
                    .username("business")
                    .password(ENCODED_PASSWORD)
                    .roles("BUSINESS")
                    .build();

            default -> throw new UsernameNotFoundException("User not found: " + username);
        };
    }
}
