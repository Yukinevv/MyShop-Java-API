package com.example.shop.integration;

import com.example.shop.dto.LoginRequest;
import com.example.shop.dto.RegisterRequest;
import com.example.shop.entity.Product;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminAccessToken; // przechowamy tu token JWT admina

    @BeforeEach
    void setUp() throws Exception {
        // 1. Rejestracja usera z rolą ADMIN
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("productAdmin");
        registerRequest.setPassword("secret123");
        registerRequest.setAdmin(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk());

        // 2. Logowanie, pobieramy accessToken
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("productAdmin");
        loginRequest.setPassword("secret123");

        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String responseString = loginResult.getResponse().getContentAsString();
        var tokenMap = objectMapper.readValue(responseString, Map.class);
        adminAccessToken = (String) tokenMap.get("accessToken");
    }

    @Test
    void fullProductFlow() throws Exception {
        // 1. Tworzymy nowy produkt
        Product newProduct = new Product();
        newProduct.setName("Kamera Sony");
        newProduct.setPrice(1999.99);

        var createResult = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + adminAccessToken)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Kamera Sony"))
                .andExpect(jsonPath("$.price").value(1999.99))
                .andReturn();

        // Parsujemy ID produktu z wyniku
        String createResponse = createResult.getResponse().getContentAsString();
        Product created = objectMapper.readValue(createResponse, Product.class);
        Long createdId = created.getId();

        // 2. Pobranie listy produktów
        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + adminAccessToken)) // GET też wymaga autoryzacji w Twojej config?
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[*].id").isNotEmpty())
                .andExpect(jsonPath("$.[*].name").isNotEmpty());

        // 3. Pobranie produktu po ID
        mockMvc.perform(get("/api/products/" + createdId)
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.name").value("Kamera Sony"))
                .andExpect(jsonPath("$.price").value(1999.99));

        // 4. Usunięcie produktu
        mockMvc.perform(delete("/api/products/" + createdId)
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isNoContent());

        // 5. Teraz GET po tym ID powinien się wysypać na 4xx (np. 400, 404):
        var getAfterDelete = mockMvc.perform(get("/api/products/" + createdId)
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andReturn();

        int statusAfterDelete = getAfterDelete.getResponse().getStatus();
        // W Twojej implementacji `getProductById()` wyrzuca RuntimeException -> 400 w GlobalExceptionHandler
        // lub 404? Sprawdź. Tu wystarczy asercja:
        assertTrue(statusAfterDelete == 404 || statusAfterDelete == 400,
                "Po usunięciu, status powinien być 400 lub 404; faktycznie = " + statusAfterDelete);
    }
}
