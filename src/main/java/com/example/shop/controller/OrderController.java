package com.example.shop.controller;

import com.example.shop.dto.OrderRequest;
import com.example.shop.entity.Order;
import com.example.shop.entity.User;
import com.example.shop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Przykład "mock'owanego" usera. W realnej aplikacji
    // skorzystaj z mechanizmu autentykacji (Spring Security/JWT).
    private User getCurrentUser() {
        // Tutaj w praktyce odczytujesz z kontekstu bezpieczeństwa
        // Na potrzeby przykładu zwracamy "testowego" usera
        User user = new User();
        user.setId(1L);
        user.setUsername("demoUser");
        user.setPassword("demoPassword");
        return user;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest orderRequest) {
        User currentUser = getCurrentUser();
        Order order = orderService.createOrder(currentUser, orderRequest);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrdersForUser() {
        User currentUser = getCurrentUser();
        List<Order> orders = orderService.getAllOrdersForUser(currentUser);
        return ResponseEntity.ok(orders);
    }
}
