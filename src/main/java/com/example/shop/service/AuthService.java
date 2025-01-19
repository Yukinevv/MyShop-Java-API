package com.example.shop.service;

import com.example.shop.dto.LoginRequest;
import com.example.shop.dto.RegisterRequest;
import com.example.shop.entity.User;
import com.example.shop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // wstrzykniemy przez konstruktor

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest registerRequest) {
        Optional<User> existingUser = userRepository.findByUsername(registerRequest.getUsername());
        if (existingUser.isPresent()) {
            throw new RuntimeException("Użytkownik o podanej nazwie już istnieje");
        }

        // Hashowanie hasła
        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = new User(registerRequest.getUsername(), encodedPassword);

        // np. domyślna rola:
        user.setRole("ROLE_USER");

        return userRepository.save(user);
    }

    public User login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Nieprawidłowy login lub hasło"));

        // Sprawdzamy hasło przez passwordEncoder.matches
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("Nieprawidłowy login lub hasło");
        }

        return user;
    }
}

