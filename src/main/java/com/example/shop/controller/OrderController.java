package com.example.shop.controller;

import com.example.shop.dto.OrderRequest;
import com.example.shop.entity.Order;
import com.example.shop.entity.User;
import com.example.shop.service.AuthService;
import com.example.shop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final AuthService authService;

    public OrderController(OrderService orderService, AuthService authService) {
        this.orderService = orderService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest orderRequest) {
        User currentUser = authService.getCurrentUser(); // pobranie realnego usera z kontekstu
        Order order = orderService.createOrder(currentUser, orderRequest);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrdersForUser() {
        User currentUser = authService.getCurrentUser();
        List<Order> orders = orderService.getAllOrdersForUser(currentUser);
        return ResponseEntity.ok(orders);
    }
}
