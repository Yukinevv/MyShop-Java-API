package com.example.shop.service;

import com.example.shop.dto.OrderRequest;
import com.example.shop.entity.Order;
import com.example.shop.entity.Product;
import com.example.shop.entity.User;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public Order createOrder(User user, OrderRequest orderRequest) {
        // Pobieramy listę produktów
        List<Long> productIds = orderRequest.getProductIds();
        Set<Product> products = new HashSet<>(productRepository.findAllById(productIds));

        Order order = new Order();
        order.setUser(user);
        order.setProducts(products);

        return orderRepository.save(order);
    }

    public List<Order> getAllOrdersForUser(User user) {
        // Możemy dodać dedykowaną metodę w OrderRepository, np. findAllByUserId(user.getId())
        // Dla uproszczenia pobieramy wszystkie zamówienia i filtrujemy
        return orderRepository.findAll().stream()
                .filter(o -> o.getUser().getId().equals(user.getId()))
                .toList();
    }
}
