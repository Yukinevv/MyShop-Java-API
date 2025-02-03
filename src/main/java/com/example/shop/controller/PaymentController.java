package com.example.shop.controller;

import com.example.shop.dto.PaymentInitDto;
import com.example.shop.dto.PaymentRedirectDto;
import com.example.shop.entity.Order;
import com.example.shop.service.OrderService;
import com.example.shop.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    public PaymentController(OrderService orderService, PaymentService paymentService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    /**
     * Inicjuje płatność – zwraca link do przekierowania.
     */
    @PostMapping("/init")
    public ResponseEntity<PaymentRedirectDto> initPayment(@RequestBody PaymentInitDto paymentRequest) {
        // Pobierz zamówienie – możesz dodać metodę getOrderById w OrderService
        Order order = orderService.getOrderById(paymentRequest.getOrderId());
        PaymentRedirectDto redirectDto = paymentService.initiatePayment(order);
        return ResponseEntity.ok(redirectDto);
    }

    /**
     * Odbiera callback z bramki płatności.
     */
    @PostMapping("/notify")
    public ResponseEntity<String> paymentNotification(@RequestBody String callbackPayload) {
        paymentService.handlePaymentCallback(callbackPayload);
        return ResponseEntity.ok("OK");
    }
}
