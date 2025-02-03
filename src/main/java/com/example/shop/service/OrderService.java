package com.example.shop.service;

import com.example.shop.dto.OrderItemRequest;
import com.example.shop.dto.OrderRequest;
import com.example.shop.entity.*;
import com.example.shop.repository.CartItemRepository;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, CartItemRepository cartItemRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Transactional
    public Order createOrder(User user, OrderRequest orderRequest) {

        // Tworzymy puste zamówienie
        Order order = new Order();
        order.setUser(user);

        Set<OrderItem> items = new HashSet<>();

        for (OrderItemRequest itemReq : orderRequest.getItems()) {
            // Szukamy produktu w bazie
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Produkt nie istnieje"));

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Brak wystarczającego stanu magazynowego dla produktu: "
                        + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            // Tworzymy obiekt OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemReq.getQuantity());
            // zapamiętujemy cenę w chwili zamówienia
            orderItem.setPriceAtOrderTime(product.getPrice());

            items.add(orderItem);
        }

        order.setItems(items);
        return orderRepository.save(order);
    }

    @Transactional
    public Order finalizeOrder(User user) {

        // Pobieramy wszystkie CartItemy usera
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Koszyk jest pusty");
        }

        // Tworzymy Order
        Order order = new Order();
        order.setUser(user);

        Set<OrderItem> orderItems = new HashSet<>();

        for (CartItem ci : cartItems) {
            // Sprawdzamy, czy rezerwacja jeszcze nie wygasła
            if (ci.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Rezerwacja wygasła dla produktu: " + ci.getProduct().getName());
            }

            Product p = ci.getProduct();

//            Product product = productRepository.findById(p.getId())
//                    .orElseThrow(() -> new RuntimeException("Produkt nie istnieje"));
//
//            if (product.getStockQuantity() < p.getStockQuantity()) {
//                throw new RuntimeException("Brak wystarczającego stanu magazynowego dla produktu: "
//                        + product.getName());
//            }
//            product.setStockQuantity(product.getStockQuantity() - p.getStockQuantity());
//            productRepository.save(product);

            // Tworzymy OrderItem
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setProduct(p);
            oi.setQuantity(ci.getQuantity());
            oi.setPriceAtOrderTime(p.getPrice());
            orderItems.add(oi);
        }
        order.setItems(orderItems);

        // Zapisujemy zamówienie
        orderRepository.save(order);

        // Czyścimy koszyk
        for (CartItem ci : cartItems) {
            cartItemRepository.delete(ci);
        }

        return order;
    }

    public List<Order> getAllOrdersForUser(User user) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getUser().getId().equals(user.getId()))
                .toList();
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Zamówienie o id " + orderId + " nie istnieje"));
    }
}
