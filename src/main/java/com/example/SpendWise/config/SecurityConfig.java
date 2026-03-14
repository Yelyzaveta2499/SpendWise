package com.example.SpendWise.config;

import com.example.SpendWise.security.JwtAuthenticationFilter;
import com.example.SpendWise.security.JwtUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtUserDetailsService jwtUserDetailsService;

	public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
						  JwtUserDetailsService jwtUserDetailsService) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.jwtUserDetailsService = jwtUserDetailsService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorize -> authorize
				// Public: login pages + static assets + JWT login endpoint
				.requestMatchers("/login", "/api/auth/login", "/auth/login", "/css/**", "/js/**", "/images/**").permitAll()
				.requestMatchers("/api/expenses/**").authenticated()
				.requestMatchers("/api/goals/**").authenticated()
				.requestMatchers("/api/chat").authenticated()
				.anyRequest().authenticated()
			)
			// Form login stays exactly the same → browser UI unchanged
			.formLogin(formLogin -> formLogin
				.loginPage("/login")
				.defaultSuccessUrl("/post-login", true)
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout=true")
				.permitAll()
			)
			.csrf(csrf -> csrf.disable())
			// JWT filter runs before Spring Security's username/password filter
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * DaoAuthenticationProvider wires JwtUserDetailsService + BCrypt together.
	 * Used by AuthenticationManager for both form login and /auth/login.
	 */
	@Bean
	public DaoAuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(jwtUserDetailsService);
		provider.setPasswordEncoder(passwordEncoder());
		return provider;
	}

	/**
	 * Exposes AuthenticationManager so AuthController can inject it.
	 */
	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}

