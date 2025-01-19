package com.example.shop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    // Relacja wiele zamówień -> jeden użytkownik
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Relacja wiele zamówień -> wiele produktów
    @ManyToMany
    @JoinTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private Set<Product> products = new HashSet<>();

    public Order() {
        this.createdAt = LocalDateTime.now();
    }

    public Order(User user, Set<Product> products) {
        this.user = user;
        this.products = products;
        this.createdAt = LocalDateTime.now();
    }
}
