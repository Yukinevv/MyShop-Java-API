package com.example.shop.controller;

import com.example.shop.dto.AddToCartRequest;
import com.example.shop.entity.CartItem;
import com.example.shop.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Dodaje item do koszyka usera.
     * Przykładowe body:
     * {
     *   "userId": 1,
     *   "productId": 10,
     *   "quantity": 2
     * }
     */
    @PostMapping
    public ResponseEntity<CartItem> addToCart(@RequestBody AddToCartRequest request) {
        CartItem item = cartService.addToCart(request.getUserId(), request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(item);
    }

    /**
     * Zwraca listę pozycji w koszyku usera.
     * (userId przekazywany np. jako query param: ?userId=1,
     *  lub w tokenie JWT, itp.)
     */
    @GetMapping
    public ResponseEntity<List<CartItem>> getCartItems(@RequestParam Long userId) {
        List<CartItem> items = cartService.getCartItems(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Usuwa pojedynczą pozycję z koszyka (po ID cartItem).
     */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Long cartItemId) {
        cartService.removeFromCart(cartItemId);
        return ResponseEntity.noContent().build();
    }
}
