package com.example.shop.controller;

import com.example.shop.dto.LoginRequest;
import com.example.shop.dto.RegisterRequest;
import com.example.shop.entity.RefreshToken;
import com.example.shop.entity.User;
import com.example.shop.security.JwtService;
import com.example.shop.service.AuthService;
import com.example.shop.service.RefreshTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;  // wstrzykujemy nasz JwtService
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest registerRequest) {
        // Rejestracja
        User user = authService.register(registerRequest);
        // Możemy od razu wygenerować token po rejestracji, albo tylko zwrócić "OK"
        return ResponseEntity.ok("Zarejestrowano użytkownika: " + user.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // Weryfikacja użytkownika i hasła
        User user = authService.login(loginRequest);

        // Generujemy access token (krótkie życie, np. 15m) - robisz to w JwtService
        String accessToken = jwtService.generateToken(user.getUsername());

        // Generujemy refresh token (w bazie) - robimy to w refreshTokenService
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        // Zwracamy oba tokeny
        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken.getToken());

        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Missing refreshToken");
        }

        refreshTokenService.deleteRefreshToken(refreshToken);
        return ResponseEntity.ok("Refresh token invalidated");
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> requestBody) {
        String refreshToken = requestBody.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Missing refreshToken");
        }

        // Walidujemy refresh token w bazie
        Optional<RefreshToken> refreshTokenOpt = refreshTokenService.validateRefreshToken(refreshToken);
        if (refreshTokenOpt.isEmpty()) {
            // niepoprawny lub przeterminowany
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }

        // Wydobywamy usera z refresh tokenu
        RefreshToken validRefresh = refreshTokenOpt.get();
        User user = validRefresh.getUser();

        // Generujemy nowy access token
        String newAccessToken = jwtService.generateToken(user.getUsername());

        // (opcjonalnie) Od razu generujemy nowy refresh token?
        // RefreshToken newRefresh = refreshTokenService.createRefreshToken(user);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", newAccessToken);
        // tokens.put("refreshToken", newRefresh.getToken()); // jeśli chcesz "one-time" refresh
        return ResponseEntity.ok(tokens);
    }
}

