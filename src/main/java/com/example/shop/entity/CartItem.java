package com.example.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Kto dodał do koszyka
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Jaki produkt
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Ilość sztuk
    private int quantity;

    // Kiedy zarezerwowano
    private LocalDateTime reservedAt;

    // Kiedy rezerwacja traci ważność
    private LocalDateTime expiresAt;

    public CartItem() {}

    public CartItem(User user, Product product, int quantity, LocalDateTime reservedAt, LocalDateTime expiresAt) {
        this.user = user;
        this.product = product;
        this.quantity = quantity;
        this.reservedAt = reservedAt;
        this.expiresAt = expiresAt;
    }
}
