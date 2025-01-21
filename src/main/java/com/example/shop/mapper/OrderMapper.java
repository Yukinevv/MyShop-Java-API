package com.example.shop.mapper;

import com.example.shop.dto.OrderDto;
import com.example.shop.dto.OrderItemDto;
import com.example.shop.entity.Order;
import com.example.shop.entity.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderDto toDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());

        if (order.getUser() != null) {
            dto.setUserId(order.getUser().getId());
            dto.setUsername(order.getUser().getUsername());
        }

        // Mapujemy kolekcję items -> listę OrderItemDto
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(OrderMapper::mapOrderItemToDto) // statyczna metoda poniżej
                .collect(Collectors.toList());

        dto.setItems(itemDtos);
        return dto;
    }

    private static OrderItemDto mapOrderItemToDto(OrderItem item) {
        OrderItemDto itemDto = new OrderItemDto();
        if (item.getProduct() != null) {
            itemDto.setProductId(item.getProduct().getId());
            itemDto.setProductName(item.getProduct().getName()); // jeśli chcemy nazwę produktu
        }
        itemDto.setPriceAtOrderTime(item.getPriceAtOrderTime() != null ? item.getPriceAtOrderTime() : 0.0);
        itemDto.setQuantity(item.getQuantity());
        return itemDto;
    }
}
