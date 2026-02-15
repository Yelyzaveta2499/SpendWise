package com.example.SpendWise.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
				.requestMatchers("/api/expenses/**").authenticated()
				.anyRequest().authenticated()
			)
			.formLogin(formLogin -> formLogin
				.defaultSuccessUrl("/post-login", true)
				.permitAll()
			)
			.logout(logout -> logout
				.logoutUrl("/logout")
				.logoutSuccessUrl("/login?logout=true")
				.permitAll()
			)
			.csrf(csrf -> csrf.disable());

		return http.build();
	}

	@Bean
	public UserDetailsService userDetailsService() {
		UserDetails user = User.builder()
			.username("indiv")
			.password(passwordEncoder().encode("password"))
			.roles("INDIVIDUAL")
			.build();

		UserDetails business = User.builder()
			.username("business")
			.password(passwordEncoder().encode("password"))
			.roles("BUSINESS")
			.build();

		UserDetails kids = User.builder()
				.username("kid")
				.password(passwordEncoder().encode("password"))
				.roles("KIDS")
				.build();

		return new InMemoryUserDetailsManager(user, business, kids);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
