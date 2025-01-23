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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RefreshTokenIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String accessToken;
    private String refreshToken;

    @BeforeEach
    void setUp() throws Exception {
        // Rejestracja usera
        RegisterRequest regReq = new RegisterRequest();
        regReq.setUsername("refreshTester");
        regReq.setPassword("secret123");
        regReq.setAdmin(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk());

        // Logowanie usera
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("refreshTester");
        loginReq.setPassword("secret123");

        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andReturn();

        var loginJson = loginResult.getResponse().getContentAsString();
        Map<?,?> tokenMap = objectMapper.readValue(loginJson, Map.class);
        accessToken = tokenMap.get("accessToken").toString();
        refreshToken = tokenMap.get("refreshToken").toString();
    }

    @Test
    void refreshTokenFlow() throws Exception {
        // 1) Odświeżanie
        var bodyMap = Map.of("refreshToken", refreshToken);
        var refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyMap)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String refreshJson = refreshResult.getResponse().getContentAsString();
        Map<?,?> refreshMap = objectMapper.readValue(refreshJson, Map.class);
        String newAccessToken = refreshMap.get("accessToken").toString();
        assertNotNull(newAccessToken, "Powinniśmy dostać nowy accessToken");

        // 2) Wylogowanie (unieważniamy refreshToken w bazie)
        var logoutBody = Map.of("refreshToken", refreshToken);
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(logoutBody)))
                .andExpect(status().isOk())
                .andExpect(content().string("Refresh token invalidated"));

        // 3) Ponowne odświeżenie tym samym tokenem -> 401 Unauthorized
        var secondRefreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bodyMap)))
                .andReturn();

        int status = secondRefreshResult.getResponse().getStatus();
        // spodziewamy się 401
        org.junit.jupiter.api.Assertions.assertEquals(401, status,
                "Po wylogowaniu refresh token powinien być nieaktywny => 401");
    }
}
