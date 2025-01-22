package com.example.shop.controller;

import com.example.shop.dto.LoginRequest;
import com.example.shop.dto.RegisterRequest;
import com.example.shop.entity.RefreshToken;
import com.example.shop.entity.User;
import com.example.shop.exception.GlobalExceptionHandler;
import com.example.shop.security.JwtService;
import com.example.shop.service.AuthService;
import com.example.shop.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        // Budujemy MockMvc w trybie "standalone",
        // wskazujemy na nasz AuthController i ewentualnie globalny handler wyjątków
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler()) // jeśli mamy globalną obsługę błędów
                .build();
    }

    @Test
    void register_Success() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("secret123");

        User userMock = new User("testuser", "encodedPass");
        userMock.setId(100L);

        when(authService.register(any(RegisterRequest.class))).thenReturn(userMock);

        // when + then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                   {
                     "username": "testuser",
                     "password": "secret123"
                   }
                """))
                .andExpect(status().isOk())
                //.andExpect(content().string("Zarejestrowano użytkownika: testuser"))
                .andExpect(jsonPath("$.message").value("Zarejestrowano użytkownika: testuser"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void register_ValidationError() throws Exception {
        // Pusty username -> błąd walidacji (400)
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                   {
                     "username": "",
                     "password": "123"
                   }
                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success() throws Exception {
        // given
        User mockUser = new User("testuser", "encoded123");
        when(authService.login(any(LoginRequest.class))).thenReturn(mockUser);

        when(jwtService.generateToken("testuser")).thenReturn("mockAccessToken");

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken("mockRefreshToken");
        when(refreshTokenService.createRefreshToken(mockUser)).thenReturn(refreshToken);

        // when + then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                   {
                     "username": "testuser",
                     "password": "secret123"
                   }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mockAccessToken"))
                .andExpect(jsonPath("$.refreshToken").value("mockRefreshToken"));

        verify(authService).login(any(LoginRequest.class));
        verify(jwtService).generateToken(eq("testuser"));
        verify(refreshTokenService).createRefreshToken(mockUser);
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        // given
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Nieprawidłowy login lub hasło"));

        // when
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                   {
                     "username": "baduser",
                     "password": "badpass"
                   }
                """))
                .andExpect(status().isBadRequest())
                // w GlobalExceptionHandler może zwracać np. RUNTIME_ERROR, sprawdzamy
                .andExpect(result -> assertEquals(
                        "Nieprawidłowy login lub hasło",
                        result.getResolvedException().getMessage())
                );

        verify(authService).login(any(LoginRequest.class));
    }
}
