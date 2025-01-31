package com.example.shop.service;

import com.example.shop.entity.CartItem;
import com.example.shop.entity.Product;
import com.example.shop.entity.User;
import com.example.shop.repository.CartItemRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {
    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartService cartService;

    private Product sampleProduct;

    private User testUser;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product("Sample Product", 100.0, 10);
        sampleProduct.setId(1L);

        testUser = new User("testUser", "secret123", "ROLE_USER");
        testUser.setId(2L);
    }

    @Test
    void addToCart_ReturnsCartItem() {
        // given
        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
           Product p = inv.getArgument(0);
           return p;
        });
        when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> {
            CartItem ci = inv.getArgument(0);
            ci.setId(3L);
            return ci;
        });

        // when
        CartItem cartItem = cartService.addToCart(2L, 1L, 5);

        // then
        // asercje + verify operacji na repozytoriach
        assertNotNull(cartItem);
        assertEquals(3L, cartItem.getId());
        assertEquals(2L, cartItem.getUser().getId());
        assertEquals(1L, cartItem.getProduct().getId());
        assertEquals(5, cartItem.getQuantity());
        assertNotNull(cartItem.getReservedAt());
        assertNotNull(cartItem.getExpiresAt());

        verify(userRepository).findById(2L);
        verify(productRepository).findById(1L);
        verify(productRepository).save(sampleProduct);
        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void getCartItems_ReturnsListCartItems() {
        // given
        CartItem testItem = new CartItem(testUser, sampleProduct, 2,
                LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        testItem.setId(1L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
        when(cartItemRepository.findByUser(testUser)).thenReturn(List.of(testItem));

        // when
        List<CartItem> cartItems = cartService.getCartItems(2L);

        // then
        assertNotNull(cartItems);
        assertEquals(1, cartItems.size());
        CartItem firstItem = cartItems.getFirst();

        assertEquals(1L, firstItem.getId());
        assertEquals(2L, firstItem.getUser().getId());
        assertEquals(1L, firstItem.getProduct().getId());
        assertEquals(2, firstItem.getQuantity());
        assertNotNull(firstItem.getReservedAt());
        assertNotNull(firstItem.getExpiresAt());

        verify(userRepository, times(1)).findById(2L);
        verify(cartItemRepository).findByUser(testUser);
    }
}
