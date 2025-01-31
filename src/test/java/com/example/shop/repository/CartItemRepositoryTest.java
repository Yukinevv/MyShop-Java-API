package com.example.shop.repository;

import com.example.shop.entity.CartItem;
import com.example.shop.entity.Product;
import com.example.shop.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "encodedPass");
        testUser.setRole("ROLE_USER");
        userRepository.save(testUser);

        testProduct1 = new Product("Laptop", 3000.0);
        productRepository.save(testProduct1);

        testProduct2 = new Product("Smartphone", 1500.0);
        productRepository.save(testProduct2);
    }

    @Test
    void saveCartItem_Success() {
        // given
        CartItem cartItem = new CartItem();
        cartItem.setProduct(testProduct1);
        cartItem.setUser(testUser);
        cartItem.setQuantity(5);
        cartItem.setReservedAt(LocalDateTime.now());
        cartItem.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        // when
        CartItem saved = cartItemRepository.save(cartItem);

        // then
        assertNotNull(saved.getId(), "ID should be generated");
        assertEquals(testUser.getId(), saved.getUser().getId(), "CartItem should be linked to testUser");
        assertEquals(testProduct1.getId(), saved.getProduct().getId(), "CartItem should be linked to testProduct1");
        assertEquals(5, saved.getQuantity());
        assertNotNull(saved.getReservedAt(), "ReservedAt should not be null");
        assertNotNull(saved.getExpiresAt(), "ExpiresAt should not be null");
    }

    @Test
    void findById_ReturnsCartItem_WhenExists() {
        // given
        CartItem cartItem = new CartItem();
        cartItem.setProduct(testProduct2);
        cartItem.setUser(testUser);
        cartItem.setQuantity(10);
        cartItem.setReservedAt(LocalDateTime.now());
        cartItem.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        cartItemRepository.save(cartItem);

        // when
        Optional<CartItem> foundOpt = cartItemRepository.findById(cartItem.getId());

        // then
        assertTrue(foundOpt.isPresent());
        CartItem found = foundOpt.get();

        assertNotNull(found.getId(), "ID should be generated");
        assertEquals(testUser.getId(), found.getUser().getId(), "CartItem should be linked to testUser");
        assertEquals(testProduct2.getId(), found.getProduct().getId(), "CartItem should be linked to testProduct2");
        assertEquals(10, found.getQuantity());
        assertNotNull(found.getReservedAt(), "ReservedAt should not be null");
        assertNotNull(found.getExpiresAt(), "ExpiresAt should not be null");
    }

    @Test
    void findById_ReturnsEmpty_WhenNotExists() {
        // given - no cartItem with ID = 9999

        // when
        Optional<CartItem> foundOpt = cartItemRepository.findById(9999L);

        // then
        assertTrue(foundOpt.isEmpty());
    }

    @Test
    void findByUser_ReturnsCartItems_WhenExists() {
        // given
        CartItem cartItem = new CartItem();
        cartItem.setProduct(testProduct2);
        cartItem.setUser(testUser);
        cartItem.setQuantity(10);
        cartItem.setReservedAt(LocalDateTime.now());
        cartItem.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        cartItemRepository.save(cartItem);

        // when
        List<CartItem> foundOpt = cartItemRepository.findByUser(cartItem.getUser());

        // then
        assertNotNull(foundOpt.getFirst());
        CartItem found = foundOpt.getFirst();

        assertNotNull(found.getId(), "ID should be generated");
        assertEquals(testUser.getId(), found.getUser().getId(), "CartItem should be linked to testUser");
        assertEquals(testProduct2.getId(), found.getProduct().getId(), "CartItem should be linked to testProduct2");
        assertEquals(10, found.getQuantity());
        assertNotNull(found.getReservedAt(), "ReservedAt should not be null");
        assertNotNull(found.getExpiresAt(), "ExpiresAt should not be null");
    }

    @Test
    void findAllExpired_ReturnsCartItems_WhenExists() {
        // given
        CartItem cartItem = new CartItem();
        cartItem.setProduct(testProduct1);
        cartItem.setUser(testUser);
        cartItem.setQuantity(10);
        cartItem.setReservedAt(LocalDateTime.now());
        cartItem.setExpiresAt(LocalDateTime.now().minusMinutes(2));
        cartItemRepository.save(cartItem);

        CartItem cartItem2 = new CartItem();
        cartItem2.setProduct(testProduct2);
        cartItem2.setUser(testUser);
        cartItem2.setQuantity(5);
        cartItem2.setReservedAt(LocalDateTime.now());
        cartItem2.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        cartItemRepository.save(cartItem2);

        // when
        List<CartItem> foundOpt = cartItemRepository.findAllExpired(LocalDateTime.now());

        // then
        assertEquals(1, foundOpt.size());

        assertNotNull(foundOpt.getFirst());
        CartItem found = foundOpt.getFirst();

        assertNotNull(found.getId(), "ID should be generated");
        assertEquals(testUser.getId(), found.getUser().getId(), "CartItem should be linked to testUser");
        assertEquals(testProduct1.getId(), found.getProduct().getId(), "CartItem should be linked to testProduct1");
        assertEquals(10, found.getQuantity());
        assertNotNull(found.getReservedAt(), "ReservedAt should not be null");
        assertNotNull(found.getExpiresAt(), "ExpiresAt should not be null");
    }

    @Test
    void findAll_ReturnsListOfCartItems() {
        // given
        CartItem cartItem = new CartItem();
        cartItem.setProduct(testProduct1);
        cartItem.setUser(testUser);
        cartItem.setQuantity(5);
        cartItem.setReservedAt(LocalDateTime.now());
        cartItem.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        cartItemRepository.save(cartItem);

        CartItem cartItem2 = new CartItem();
        cartItem2.setProduct(testProduct2);
        cartItem2.setUser(testUser);
        cartItem2.setQuantity(10);
        cartItem2.setReservedAt(LocalDateTime.now());
        cartItem2.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        cartItemRepository.save(cartItem2);

        // when
        List<CartItem> allCartItems = cartItemRepository.findAll();

        // then
        assertEquals(2, allCartItems.size());
    }

    @Test
    void deleteById_RemovesCartItem() {
        // given
        CartItem cartItem = new CartItem();
        cartItem.setProduct(testProduct2);
        cartItem.setUser(testUser);
        cartItem.setQuantity(10);
        cartItem.setReservedAt(LocalDateTime.now());
        cartItem.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        cartItemRepository.save(cartItem);

        // when
        cartItemRepository.deleteById(cartItem.getId());

        // then
        Optional<CartItem> foundOpt = cartItemRepository.findById(cartItem.getId());
        assertTrue(foundOpt.isEmpty(), "CartItem should be removed from repo");
    }
}
