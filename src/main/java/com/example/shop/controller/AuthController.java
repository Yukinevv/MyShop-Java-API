package com.example.shop.controller;

import com.example.shop.dto.LoginRequest;
import com.example.shop.dto.RegisterRequest;
import com.example.shop.entity.User;
import com.example.shop.security.JwtService;
import com.example.shop.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;  // wstrzykujemy nasz JwtService

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        // Rejestracja
        User user = authService.register(registerRequest);
        // Możemy od razu wygenerować token po rejestracji, albo tylko zwrócić "OK"
        return ResponseEntity.ok("Zarejestrowano użytkownika: " + user.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        // Weryfikacja nazwy użytkownika i hasła
        User user = authService.login(loginRequest);

        // Generujemy token
        String jwtToken = jwtService.generateToken(user.getUsername());

        // Zwracamy token w formie JSON lub jako zwykły string
        return ResponseEntity.ok(jwtToken);
    }
}

