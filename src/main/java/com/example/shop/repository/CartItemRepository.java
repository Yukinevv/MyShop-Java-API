package com.example.shop.repository;

import com.example.shop.entity.CartItem;
import com.example.shop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // wszystkie pozycje koszyka danego usera
    List<CartItem> findByUser(User user);

    // wyszukaj wszystkie, którym już upłynął termin rezerwacji
    @Query("SELECT c FROM CartItem c WHERE c.expiresAt < :now")
    List<CartItem> findAllExpired(@Param("now") LocalDateTime now);

}
