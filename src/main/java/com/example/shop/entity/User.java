package com.example.shop.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    private String role;

    // Możesz dodać więcej pól, np. email, imię, nazwisko, rolę, itp.

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
