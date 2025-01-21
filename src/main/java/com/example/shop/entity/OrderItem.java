package com.example.shop.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relacja do zamówienia
    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore
    private Order order;

    // Relacja do produktu
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    // Liczba sztuk produktu w tym zamówieniu
    private int quantity;

    // Cena w momencie zakupu,
    // by mieć historię (np. jeżeli cena produktu w bazie się zmieni)
    private Double priceAtOrderTime;
}
