package com.example.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDto {
    private Long productId;
    private String productName; // jeśli chcesz w JSON również nazwę produktu
    private double priceAtOrderTime;
    private int quantity;
}
