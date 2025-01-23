package com.example.shop.integration;

import com.example.shop.dto.LoginRequest;
import com.example.shop.dto.OrderItemRequest;
import com.example.shop.dto.OrderRequest;
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

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // token zwykłego usera do składania zamówień
    private String userToken;

    @BeforeEach
    void setUp() throws Exception {
        // Tworzymy usera, logujemy -> userToken
        userToken = createUserAndGetToken("normalUser", "secretUser");
    }

    /**
     * 1) Rejestracja z pustym username.
     *    Spodziewamy się 400 i JSON z "VALIDATION_ERROR".
     */
    @Test
    void registerWithEmptyUsername_shouldReturn400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("");  // pusto
        req.setPassword("abcd1234");
        req.setAdmin(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[0].field").value("username"))
                .andExpect(jsonPath("$.errors[0].message").exists());
        // w zależności jak budujesz errorResponse
    }

    /**
     * 2) Rejestracja z hasłem krótszym niż 6 znaków -> 400
     */
    @Test
    void registerWithShortPassword_shouldReturn400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("someUser");
        req.setPassword("abc");  // za krótkie
        req.setAdmin(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors[?(@.field == 'password')]").isNotEmpty());
    }

    /**
     * 3) Tworzenie zamówienia z pustą listą items
     *    Oczekujemy 400 i error "VALIDATION_ERROR", bo @NotEmpty na polu items.
     */
    @Test
    void createOrder_emptyItems_shouldReturn400() throws Exception {
        OrderRequest orderReq = new OrderRequest();
        orderReq.setItems(List.of()); // pusta lista

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].field").value("items"));
    }

    /**
     * 4) Tworzenie zamówienia z quantity=0 w items -> błąd @Min(1)
     */
    @Test
    void createOrder_zeroQuantity_shouldReturn400() throws Exception {
        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setProductId(123L);
        itemReq.setQuantity(0); // niepoprawne

        OrderRequest orderReq = new OrderRequest();
        orderReq.setItems(List.of(itemReq));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + userToken)
                        .content(objectMapper.writeValueAsString(orderReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                // sprawdzamy czy w errors jest mention o "quantity"
                .andExpect(jsonPath("$.errors[?(@.field == 'items[0].quantity')]").isNotEmpty());
    }

    /*
     * Pomocnicza metoda rejestruje zwykłego usera, loguje go i zwraca token.
     */
    private String createUserAndGetToken(String username, String password) throws Exception {
        String currentTimeInMilis = String.valueOf(System.currentTimeMillis());
        String uniqueName = username + "_" + currentTimeInMilis.substring(currentTimeInMilis.length() - 5);
        RegisterRequest regReq = new RegisterRequest();
        regReq.setUsername(uniqueName);
        regReq.setPassword(password);
        regReq.setAdmin(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk());

        // logowanie
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(uniqueName);
        loginReq.setPassword(password);

        var result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        Map<?,?> responseMap = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class);
        return responseMap.get("accessToken").toString();
    }
}
