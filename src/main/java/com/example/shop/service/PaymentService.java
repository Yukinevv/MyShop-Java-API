package com.example.shop.service;

import com.example.shop.dto.PaymentRedirectDto;
import com.example.shop.entity.Order;
import com.example.shop.entity.PaymentStatus;
import com.example.shop.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class PaymentService {

    private final OrderRepository orderRepository;

    public PaymentService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Inicjuje płatność – ustawiamy status na PENDING oraz generujemy link.
     */
    @Transactional
    public PaymentRedirectDto initiatePayment(Order order) {
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("To zamówienie jest już opłacone.");
        }

        // Ustaw status na PENDING
        order.setPaymentStatus(PaymentStatus.PENDING);

        // Przykładowa symulacja: generujemy „fałszywy” link do płatności
        String fakePaymentUrl = "https://fake-payments.example.com/pay?orderId=" + order.getId();

        // (Opcjonalnie) możesz zapisać dodatkowo identyfikator z operatora płatności:
        order.setPaymentExternalId("FAKE-" + order.getId());

        orderRepository.save(order);

        return new PaymentRedirectDto(fakePaymentUrl);
    }

    /**
     * Obsługuje callback (webhook) z bramki płatności.
     * W tej uproszczonej implementacji przyjmujemy payload jako String,
     * parsujemy (tu symulujemy) i aktualizujemy status zamówienia.
     */
    @Transactional
    public void handlePaymentCallback(String callbackPayload) {
        // W realnym przypadku tutaj sparsujesz JSON i wyciągniesz:
        // - paymentExternalId
        // - status (np. "SUCCESS", "FAILED", itd.)
        //
        // Dla uproszczenia zakładamy:
        String externalId = "FAKE-1"; // przykładowy identyfikator
        String statusFromGateway = "SUCCESS"; // przykładowy status

        // Znajdź zamówienie na podstawie paymentExternalId
        Optional<Order> orderOpt = orderRepository.findByPaymentExternalId(externalId);
        if (orderOpt.isEmpty()) {
            throw new RuntimeException("Nie znaleziono zamówienia dla tego identyfikatora płatności.");
        }
        Order order = orderOpt.get();

        if ("SUCCESS".equalsIgnoreCase(statusFromGateway)) {
            order.setPaymentStatus(PaymentStatus.PAID);
        } else {
            order.setPaymentStatus(PaymentStatus.CANCELLED);
        }
        orderRepository.save(order);
    }
}
