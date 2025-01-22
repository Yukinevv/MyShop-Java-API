package com.example.shop.integration;

import com.example.shop.dto.LoginRequest;
import com.example.shop.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // do serializacji/deserializacji JSON

    @BeforeEach
    void setUp() {
        // Można wyczyścić bazę testową przed każdym testem,
        // np. userRepository.deleteAll(), jeśli musisz,
        // albo zostawić tak jak jest (z create-drop).
    }

    @Test
    void register_and_login_flow() throws Exception {
        // 1. Rejestracja
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("secret123");
        registerRequest.setAdmin(true);

        // Zserializowany JSON
        String regJson = objectMapper.writeValueAsString(registerRequest);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(regJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Zarejestrowano użytkownika: testuser"));

        // 2. Logowanie
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("secret123");

        String loginJson = objectMapper.writeValueAsString(loginRequest);

        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        // 3. Parsowanie JSON i sprawdzanie tokenów
        String responseString = loginResult.getResponse().getContentAsString();

        // Przykładowy obiekt mapujący
        var tokenMap = objectMapper.readValue(responseString, Map.class);
        String accessToken = (String) tokenMap.get("accessToken");
        String refreshToken = (String) tokenMap.get("refreshToken");

        assertNotNull(accessToken, "Access token should not be null");
        assertNotNull(refreshToken, "Refresh token should not be null");

        // 4. (opcjonalnie) Możemy przetestować wywołanie endpointu wymagającego autoryzacji.
        // Np. GET /api/products – w Twojej konfiguracji jest .authenticated() do pobrania listy
        // Jedynie musimy wysłać nagłówek Authorization: Bearer ...
        mockMvc.perform(post("/api/products") // lub GET, w zależności od endpointu
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "name": "Laptop Dell",
                      "price": 2500.0
                    }
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Laptop Dell"));
    }
}
