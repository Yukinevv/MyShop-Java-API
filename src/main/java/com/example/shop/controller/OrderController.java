package com.example.shop.controller;

import com.example.shop.dto.OrderDto;
import com.example.shop.dto.OrderRequest;
import com.example.shop.entity.Order;
import com.example.shop.entity.User;
import com.example.shop.mapper.OrderMapper;
import com.example.shop.service.AuthService;
import com.example.shop.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<OrderDto> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        User currentUser = authService.getCurrentUser(); // pobranie realnego usera z kontekstu
        Order order = orderService.createOrder(currentUser, orderRequest);

        // mapujemy encję do DTO
        OrderDto orderDto = OrderMapper.toDto(order);
        return ResponseEntity.ok(orderDto);
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrdersForUser() {
        User currentUser = authService.getCurrentUser();
        List<Order> orders = orderService.getAllOrdersForUser(currentUser);

        // mapujemy listę encji do listy DTO
        List<OrderDto> dtos = orders.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }
}
