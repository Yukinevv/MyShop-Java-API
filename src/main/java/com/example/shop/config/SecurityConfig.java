package com.example.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        // np. otwarte endpointy
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // reszta wymaga uwierzytelnienia
                        .anyRequest().authenticated()
                )
                // Wyłączenie CSRF w stylu lambda DSL
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}

