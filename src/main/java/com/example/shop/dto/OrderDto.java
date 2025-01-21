package com.example.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDto {
    private Long id;
    private LocalDateTime createdAt;
    private Long userId;
    private String username; // jeśli chcesz, by w JSON było też pole z nazwą usera

    private List<OrderItemDto> items; // lista pozycji
}
