package com.example.shop.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {
    @NotEmpty(message = "Lista produktów nie może być pusta")
    @Valid // włącza walidację wewnętrznej listy
    private List<OrderItemRequest> items;
}
