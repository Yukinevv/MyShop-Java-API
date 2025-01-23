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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // token admina do tworzenia produktów
    private String adminToken;

    // token zwykłego usera do składania zamówień
    private String userToken;

    // IDs produktów, które w testach będą zamawiane
    private Long productAId;
    private Long productBId;

    @BeforeEach
    void setUp() throws Exception {
        // 1) Tworzymy admina, logujemy się -> mamy adminToken
        adminToken = createAdminAndGetToken("orderAdmin", "secretAdmin");

        // 2) Tworzymy 2 produkty (do zamówień)
        productAId = createProduct("Klawiatura", 120.0);
        productBId = createProduct("Mysz", 60.0);

        // 3) Tworzymy usera, logujemy -> userToken
        userToken = createUserAndGetToken("normalUser", "secretUser");
    }

    /**
     * Przykładowy test zamawiania 2 produktów przez zwykłego usera.
     * Sprawdzamy czy zamówienie się zapisuje i czy GET /api/orders je zwraca.
     */
    @Test
    void test_createAndGetOrders() throws Exception {
        // 1) Tworzymy żądanie z 2 pozycjami
        OrderItemRequest item1 = new OrderItemRequest();
        item1.setProductId(productAId);
        item1.setQuantity(2);

        OrderItemRequest item2 = new OrderItemRequest();
        item2.setProductId(productBId);
        item2.setQuantity(1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(item1, item2));

        // 2) Wywołujemy POST /api/orders z tokenem usera
        var createOrderResult = mockMvc.perform(post("/api/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.items[0].productId").value(productAId))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[1].productId").value(productBId))
                .andExpect(jsonPath("$.items[1].quantity").value(1))
                .andReturn();

        // Parsujemy ID z JSON, by móc ewentualnie sprawdzić coś więcej
        String createResponse = createOrderResult.getResponse().getContentAsString();
        Map<?,?> createdOrderMap = objectMapper.readValue(createResponse, Map.class);
        Long createdOrderId = Long.valueOf(createdOrderMap.get("id").toString());

        // 3) GET /api/orders -> sprawdzamy listę zamówień usera
        var getOrdersResult = mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                // .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Odczytujemy tablicę JSON, sprawdzamy czy jest tam 1 zamówienie
        String ordersJson = getOrdersResult.getResponse().getContentAsString();
        List<Map<?,?>> ordersList = objectMapper.readValue(ordersJson, List.class);

        assertEquals(1, ordersList.size(),
                "Powinno być 1 zamówienie w /api/orders dla tego usera.");

        // Ewentualnie sprawdzamy ID:
        Long returnedId = Long.valueOf(ordersList.get(0).get("id").toString());
        assertEquals(createdOrderId, returnedId,
                "ID zamówienia z GET powinno być takie samo jak utworzone.");
    }

    /*
     * Pomocnicza metoda rejestruje admina, loguje go i zwraca token.
     */
    private String createAdminAndGetToken(String username, String password) throws Exception {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setUsername(username);
        regReq.setPassword(password);
        regReq.setAdmin(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk());

        // logowanie
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username);
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

    /*
     * Pomocnicza metoda rejestruje zwykłego usera, loguje go i zwraca token.
     */
    private String createUserAndGetToken(String username, String password) throws Exception {
        RegisterRequest regReq = new RegisterRequest();
        regReq.setUsername(username);
        regReq.setPassword(password);
        regReq.setAdmin(false);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isOk());

        // logowanie
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(username);
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

    /*
     * Pomocnicza metoda tworzy produkt w bazie przez /api/products (admin jest potrzebny).
     * Zwraca ID produktu z JSON-a.
     */
    private Long createProduct(String name, double price) throws Exception {
        String productJson = """
                {
                  "name": "%s",
                  "price": %f
                }
                """.formatted(name, price);

        var result = mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isOk())
                .andReturn();

        Map<?,?> productMap = objectMapper.readValue(
                result.getResponse().getContentAsString(), Map.class);

        return Long.valueOf(productMap.get("id").toString());
    }

}
