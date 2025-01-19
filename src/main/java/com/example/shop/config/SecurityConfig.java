package com.example.shop.config;

import com.example.shop.security.JwtAuthenticationFilter;
import com.example.shop.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            UserDetailsServiceImpl userDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // rejestrujemy nasz AuthenticationProvider
        http.authenticationProvider(daoAuthenticationProvider());

        http
                .csrf(csrf -> csrf.disable())  // do testów Postmanem
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                        // tworzenie produktów dostępne tylko dla roli ADMIN:
                        .requestMatchers(HttpMethod.POST, "/api/products").hasRole("ADMIN")

                        // pobieranie produktów dostępne dla zalogowanych (np. hasAnyRole("USER","ADMIN"))
                        .requestMatchers(HttpMethod.GET, "/api/products").authenticated()

                        // reszta zabezpieczona w standardowy sposób
                        .anyRequest().authenticated()
                )
                // Możesz użyć .httpBasic(Customizer.withDefaults()) jeśli chcesz testować Basic Auth
                // lub wyłączyć to całkowicie i działać tylko na JWT
                .httpBasic(Customizer.withDefaults());

        // rejestracja filtra JWT przed UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    // Tylko w razie potrzeby, np. do manualnego uwierzytelniania
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .authenticationProvider(daoAuthenticationProvider())
                .build();
    }
}

//@Configuration
//public class SecurityConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                // Pozwala na dostęp do każdego endpointu bez logowania
//                .authorizeHttpRequests(authorize -> authorize
//                        .anyRequest().permitAll()
//                )
//                // Wyłącz CSRF, by nie wymagał tokenu CSRF w żądaniach POST/PUT
//                .csrf(csrf -> csrf.disable());
//
//        return http.build();
//    }
//}
