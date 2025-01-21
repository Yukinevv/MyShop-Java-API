package com.example.shop.service;

import com.example.shop.dto.LoginRequest;
import com.example.shop.dto.RegisterRequest;
import com.example.shop.entity.User;
import com.example.shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("secret123");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("secret123");
    }

    @Test
    void register_Success() {
        // given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            userArg.setId(1L);
            return userArg;
        });

        // when
        User result = authService.register(registerRequest);

        // then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("encoded-pass", result.getPassword());
        assertEquals("ROLE_USER", result.getRole()); // isAdmin() = false by default
        assertEquals(1L, result.getId());

        verify(userRepository).findByUsername("testuser");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("secret123");
    }

    @Test
    void register_ExistingUser_ThrowsException() {
        // given
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(new User("testuser", "somePass")));

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(registerRequest));
        assertEquals("Użytkownik o podanej nazwie już istnieje", ex.getMessage());

        verify(userRepository).findByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void login_Success() {
        // given
        User existingUser = new User("testuser", "encoded123");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("secret123", "encoded123"))
                .thenReturn(true);

        // when
        User result = authService.login(loginRequest);

        // then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("secret123", "encoded123");
    }

    @Test
    void login_UserNotFound_ThrowsException() {
        // given
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(loginRequest));
        assertEquals("Nieprawidłowy login lub hasło", ex.getMessage());

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_PasswordMismatch_ThrowsException() {
        // given
        User existingUser = new User("testuser", "encoded123");
        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches("secret123", "encoded123"))
                .thenReturn(false);

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(loginRequest));
        assertEquals("Nieprawidłowy login lub hasło", ex.getMessage());

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("secret123", "encoded123");
    }
}
