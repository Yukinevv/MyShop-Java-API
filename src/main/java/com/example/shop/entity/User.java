package com.example.shop.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String password;

    // Możesz dodać więcej pól, np. email, imię, nazwisko, rolę, itp.

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // gettery, settery
    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // hasła w praktyce nie przechowujemy w czystej postaci
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setId(long id) {
        this.id = id;
    }
}
