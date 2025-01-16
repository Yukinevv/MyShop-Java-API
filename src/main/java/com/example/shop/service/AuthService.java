package com.example.shop.service;

import com.example.shop.dto.LoginRequest;
import com.example.shop.dto.RegisterRequest;
import com.example.shop.entity.User;
import com.example.shop.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(RegisterRequest registerRequest) {
        // Sprawdź, czy użytkownik o takiej nazwie nie istnieje
        Optional<User> existingUser = userRepository.findByUsername(registerRequest.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Użytkownik o podanej nazwie już istnieje");
        }

        // W praktyce hashowanie hasła, np. BCrypt
        User user = new User(registerRequest.getUsername(), registerRequest.getPassword());
        return userRepository.save(user);
    }

    public User login(LoginRequest loginRequest) {
        // Szukamy użytkownika
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Nieprawidłowy login lub hasło"));

        // Sprawdzamy hasło (w praktyce hashowanie i porównywanie)
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new RuntimeException("Nieprawidłowy login lub hasło");
        }

        return user;
    }
}
