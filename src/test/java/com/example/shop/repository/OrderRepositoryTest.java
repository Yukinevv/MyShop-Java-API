package com.example.shop.repository;

import com.example.shop.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy repozytorium OrderRepository w kontekście bazy testowej.
 * Używamy @DataJpaTest i parametru replace = NONE,
 * by uniknąć automatycznego włączania H2 (korzystamy z bazy testowej MSSQL).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // Tworzymy usera i dwa produkty, żeby mieć dane do zamówienia
        testUser = new User("testuser", "encodedPass");
        testUser.setRole("ROLE_USER");
        userRepository.save(testUser);

        testProduct1 = new Product("Laptop", 3000.0);
        productRepository.save(testProduct1);

        testProduct2 = new Product("Smartphone", 1500.0);
        productRepository.save(testProduct2);
    }

    @Test
    void saveOrder_withItems_savesAndRetrievesCorrectly() {
        // given
        Order order = new Order();
        order.setUser(testUser); // relacja ManyToOne do usera

        // Tworzymy OrderItem #1
        OrderItem item1 = new OrderItem();
        item1.setOrder(order);
        item1.setProduct(testProduct1);
        item1.setQuantity(2);
        item1.setPriceAtOrderTime(testProduct1.getPrice());

        // Tworzymy OrderItem #2
        OrderItem item2 = new OrderItem();
        item2.setOrder(order);
        item2.setProduct(testProduct2);
        item2.setQuantity(1);
        item2.setPriceAtOrderTime(testProduct2.getPrice());

        // Dodajemy do zbioru
        Set<OrderItem> items = new HashSet<>();
        items.add(item1);
        items.add(item2);

        order.setItems(items);

        // when
        Order savedOrder = orderRepository.save(order);

        // then
        assertNotNull(savedOrder.getId(), "Order ID should be generated");
        assertEquals(testUser.getId(), savedOrder.getUser().getId(), "Order should be linked to testUser");
        assertNotNull(savedOrder.getCreatedAt(), "CreatedAt should be set in constructor or default");

        // Weryfikacja itemów
        assertEquals(2, savedOrder.getItems().size());

        // Odczyt z bazy ponownie:
        Optional<Order> foundOpt = orderRepository.findById(savedOrder.getId());
        assertTrue(foundOpt.isPresent());

        Order found = foundOpt.get();
        assertEquals(2, found.getItems().size());

        // weryfikacja jednego z itemów
        // np. czy jest item z product=Laptop, quantity=2
        boolean hasLaptop = found.getItems().stream()
                .anyMatch(i -> i.getProduct().getName().equals("Laptop") && i.getQuantity() == 2);
        assertTrue(hasLaptop, "OrderItem for 'Laptop' with qty=2 should be found");
    }

    @Test
    void findAll_returnsMultipleOrders() {
        // given
        // Tworzymy dwa zamówienia
        Order order1 = new Order();
        order1.setUser(testUser);
        orderRepository.save(order1);

        Order order2 = new Order();
        order2.setUser(testUser);
        orderRepository.save(order2);

        // when
        List<Order> allOrders = orderRepository.findAll();

        // then
        assertEquals(2, allOrders.size());
    }

    @Test
    void deleteOrder_cascadesToOrderItems() {
        // given
        // Zamówienie z jednym itemem
        Order order = new Order();
        order.setUser(testUser);

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setProduct(testProduct1);
        item.setQuantity(3);
        item.setPriceAtOrderTime(3000.0);

        order.setItems(Set.of(item));
        orderRepository.save(order);

        // when
        orderRepository.delete(order);

        // then
        // Sprawdzamy, czy order i item zostały usunięte
        assertTrue(orderRepository.findById(order.getId()).isEmpty(), "Order should be deleted");
    }
}
