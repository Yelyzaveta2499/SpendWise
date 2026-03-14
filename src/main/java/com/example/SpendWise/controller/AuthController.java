package com.example.SpendWise.controller;

import com.example.SpendWise.security.JwtService;
import com.example.SpendWise.security.JwtUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/auth", "/auth"})
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUserDetailsService jwtUserDetailsService;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUserDetailsService jwtUserDetailsService,
                          JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtUserDetailsService = jwtUserDetailsService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

        // Delegate to AuthenticationManager which uses JwtUserDetailsService + BCrypt
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // Load full UserDetails so JwtService can build the token with authorities
        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(
                authentication.getName()
        );

        String token = jwtService.generateToken(userDetails);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    // -------------------------------------------------------------------------
    // Request / Response records
    // -------------------------------------------------------------------------

    /**
     * Login request body: { "username": "...", "password": "..." }
     */
    public record AuthRequest(String username, String password) {}

    /**
     * Login response body: { "token": "eyJ..." }
     */
    public record AuthResponse(String token) {}
}
