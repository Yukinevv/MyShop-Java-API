package com.example.shop.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequest {

    @NotNull(message = "productId nie może być null")
    private Long productId;

    @Min(value = 1, message = "Ilość sztuk musi być >= 1")
    private int quantity;
}
