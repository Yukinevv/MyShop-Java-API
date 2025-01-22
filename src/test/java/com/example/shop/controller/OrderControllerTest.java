package com.example.shop.controller;

import com.example.shop.dto.OrderDto;
import com.example.shop.dto.OrderRequest;
import com.example.shop.entity.Order;
import com.example.shop.entity.User;
import com.example.shop.exception.GlobalExceptionHandler;
import com.example.shop.mapper.OrderMapper;
import com.example.shop.service.AuthService;
import com.example.shop.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testy kontrolera OrderController w trybie "standalone",
 * z mockowaniem warstwy serwisowej (OrderService, AuthService).
 */
@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private OrderController orderController;

    private MockMvc mockMvc;

    private User mockUser;

    @BeforeEach
    void setup() {
        // Tworzymy "standalone" MockMvc
        mockMvc = MockMvcBuilders
                .standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        // Przykładowy user
        mockUser = new User();
        mockUser.setId(10L);
        mockUser.setUsername("demoUser");
        mockUser.setPassword("encoded123");
    }

    @Test
    void createOrder_Success() throws Exception {
        // given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        Order createdOrder = new Order();
        createdOrder.setId(101L);
        createdOrder.setUser(mockUser);
        createdOrder.setCreatedAt(LocalDateTime.of(2025,1,21,12,0));

        // Udajemy, że service zwraca createdOrder
        when(orderService.createOrder(eq(mockUser), any(OrderRequest.class)))
                .thenReturn(createdOrder);

        OrderDto createdOrderDto = new OrderDto();
        createdOrderDto.setId(101L);
        createdOrderDto.setCreatedAt(LocalDateTime.of(2025,1,21,12,0));
        createdOrderDto.setUserId(10L);
        createdOrderDto.setUsername("demoUser");
        // items = pusta lub cokolwiek

        // Możemy "zasymulować" działanie OrderMapper.toDto(...) w teście
        try (var mockedMapper = mockStatic(OrderMapper.class)) {
            mockedMapper.when(() -> OrderMapper.toDto(createdOrder))
                    .thenReturn(createdOrderDto);

            // Kiedy w request przesyłamy JSON:
            mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                       {
                         "items": [
                           { "productId": 1, "quantity": 2 },
                           { "productId": 3, "quantity": 5 }
                         ]
                       }
                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(101))
                    .andExpect(jsonPath("$.userId").value(10))
                    .andExpect(jsonPath("$.username").value("demoUser"))
                    .andExpect(jsonPath("$.createdAt").value("2025-01-21T12:00:00"));

            verify(authService).getCurrentUser();
            verify(orderService).createOrder(eq(mockUser), any(OrderRequest.class));
            mockedMapper.verify(() -> OrderMapper.toDto(createdOrder));
        }
    }

    @Test
    void createOrder_ValidationError() throws Exception {
        // given
        // Zakładamy, że OrderRequest wymaga `items` (nie może być puste).
        // Wysyłamy pustą listę
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                   {
                     "items": []
                   }
                """))
                .andExpect(status().isBadRequest());

        // bo w OrderRequest mamy np. `@NotEmpty(message=...)` na liście items
        verifyNoInteractions(authService, orderService);
    }

    @Test
    void getAllOrdersForUser_Success() throws Exception {
        // given
        when(authService.getCurrentUser()).thenReturn(mockUser);

        // Zwrot z service
        Order order1 = new Order();
        order1.setId(1000L);
        order1.setUser(mockUser);
        Order order2 = new Order();
        order2.setId(2000L);
        order2.setUser(mockUser);

        when(orderService.getAllOrdersForUser(mockUser))
                .thenReturn(List.of(order1, order2));

        // Symulujemy mapowanie encji -> DTO
        OrderDto dto1 = new OrderDto();
        dto1.setId(1000L);
        dto1.setUserId(10L);
        dto1.setUsername("demoUser");

        OrderDto dto2 = new OrderDto();
        dto2.setId(2000L);
        dto2.setUserId(10L);
        dto2.setUsername("demoUser");

        try (var mockedMapper = mockStatic(OrderMapper.class)) {
            mockedMapper.when(() -> OrderMapper.toDto(order1)).thenReturn(dto1);
            mockedMapper.when(() -> OrderMapper.toDto(order2)).thenReturn(dto2);

            // when
            mockMvc.perform(get("/api/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1000))
                    .andExpect(jsonPath("$[1].id").value(2000))
                    .andExpect(jsonPath("$[0].username").value("demoUser"))
                    .andExpect(jsonPath("$[1].username").value("demoUser"));

            verify(authService).getCurrentUser();
            verify(orderService).getAllOrdersForUser(mockUser);

            // weryfikacja, czy mapper został wywołany dwa razy
            mockedMapper.verify(() -> OrderMapper.toDto(order1));
            mockedMapper.verify(() -> OrderMapper.toDto(order2));
        }
    }
}
