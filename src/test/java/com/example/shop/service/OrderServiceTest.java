package com.example.shop.service;

import com.example.shop.dto.OrderItemRequest;
import com.example.shop.dto.OrderRequest;
import com.example.shop.entity.*;
import com.example.shop.repository.OrderRepository;
import com.example.shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(10L);
        testUser.setUsername("testUser");
        testUser.setPassword("encoded123");
        testUser.setRole("ROLE_USER");
    }

    @Test
    void createOrder_Success() {
        // given
        Product product1 = new Product();
        product1.setId(100L);
        product1.setPrice(50.0);

        Product product2 = new Product();
        product2.setId(200L);
        product2.setPrice(100.0);

        when(productRepository.findById(100L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(200L)).thenReturn(Optional.of(product2));

        // Zwracamy zamówienie z ID 999 po zapisie
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(999L);
            return o;
        });

        OrderItemRequest itemReq1 = new OrderItemRequest();
        itemReq1.setProductId(100L);
        itemReq1.setQuantity(2);

        OrderItemRequest itemReq2 = new OrderItemRequest();
        itemReq2.setProductId(200L);
        itemReq2.setQuantity(3);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(itemReq1, itemReq2));

        // when
        Order createdOrder = orderService.createOrder(testUser, orderRequest);

        // then
        assertNotNull(createdOrder);
        assertEquals(999L, createdOrder.getId());
        assertEquals(testUser, createdOrder.getUser());
        assertNotNull(createdOrder.getItems());
        assertEquals(2, createdOrder.getItems().size());

        // weryfikujemy, że cena i product w OrderItem są poprawne
        // np. pobieramy item z kolekcji
        List<OrderItem> itemsList = new ArrayList<>(createdOrder.getItems());
        OrderItem firstItem = itemsList.get(0);
        OrderItem secondItem = itemsList.get(1);

        // jedna pozycja powinna dotyczyć product1 (ID=100L, price=50.0)
        // druga product2 (ID=200L, price=100.0)
        if (firstItem.getProduct().getId().equals(100L)) {
            assertEquals(50.0, firstItem.getPriceAtOrderTime());
            assertEquals(2, firstItem.getQuantity());
        } else {
            assertEquals(100.0, firstItem.getPriceAtOrderTime());
            assertEquals(3, firstItem.getQuantity());
        }

        if (secondItem.getProduct().getId().equals(100L)) {
            assertEquals(50.0, secondItem.getPriceAtOrderTime());
            assertEquals(2, secondItem.getQuantity());
        } else {
            assertEquals(100.0, secondItem.getPriceAtOrderTime());
            assertEquals(3, secondItem.getQuantity());
        }

        verify(productRepository).findById(100L);
        verify(productRepository).findById(200L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_ProductNotFound_ThrowsException() {
        // given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        OrderItemRequest itemReq = new OrderItemRequest();
        itemReq.setProductId(999L);
        itemReq.setQuantity(1);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setItems(List.of(itemReq));

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(testUser, orderRequest));
        assertEquals("Produkt nie istnieje", ex.getMessage());

        verify(productRepository).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getAllOrdersForUser_ReturnsOnlyUserOrders() {
        // given
        User anotherUser = new User();
        anotherUser.setId(20L);

        Order order1 = new Order();
        order1.setId(1000L);
        order1.setUser(testUser);

        Order order2 = new Order();
        order2.setId(2000L);
        order2.setUser(anotherUser);

        Order order3 = new Order();
        order3.setId(3000L);
        order3.setUser(testUser);

        // findAll() zwraca wszystkie zamówienia, potem w serwisie filtrujemy
        when(orderRepository.findAll()).thenReturn(List.of(order1, order2, order3));

        // when
        List<Order> result = orderService.getAllOrdersForUser(testUser);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(order1));
        assertTrue(result.contains(order3));
        assertFalse(result.contains(order2));

        verify(orderRepository).findAll();
    }
}
