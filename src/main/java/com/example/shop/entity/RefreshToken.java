package com.example.shop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Sam "ciąg znaków" reprezentujący token (może to być JWT lub inny unikalny ID)
    @Column(unique = true, nullable = false)
    private String token;

    // Kiedy token wygaśnie
    private Instant expiryDate;

    // Powiązanie z użytkownikiem (OneToOne lub ManyToOne, w zależności od wymagań)
    // Załóżmy, że dany user może mieć wiele refresh tokenów
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
