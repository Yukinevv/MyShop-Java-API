package com.example.shop.entity;

public enum PaymentStatus {
    NEW,        // zamówienie utworzone, ale nieopłacone
    PENDING,    // w trakcie płacenia / oczekuje na zatwierdzenie
    PAID,       // opłacone
    CANCELLED,  // anulowane
    REFUNDED    // zwrócone
}