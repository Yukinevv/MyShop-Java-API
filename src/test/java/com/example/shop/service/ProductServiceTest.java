package com.example.shop.service;

import com.example.shop.entity.Product;
import com.example.shop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("Sample Product");
        sampleProduct.setPrice(100.0);
    }

    @Test
    void getAllProducts_ReturnsList() {
        // given
        List<Product> mockList = new ArrayList<>();
        mockList.add(sampleProduct);
        mockList.add(new Product("Another Product", 200.0));

        when(productRepository.findAll()).thenReturn(mockList);

        // when
        List<Product> result = productService.getAllProducts();

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(sampleProduct));

        verify(productRepository, times(1)).findAll();
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void getProductById_ExistingProduct_ReturnsProduct() {
        // given
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        // when
        Product result = productService.getProductById(1L);

        // then
        assertNotNull(result);
        assertEquals("Sample Product", result.getName());
        assertEquals(100.0, result.getPrice());
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_NotFound_ThrowsException() {
        // given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // when + then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.getProductById(999L));
        assertEquals("Produkt nie istnieje", ex.getMessage());

        verify(productRepository).findById(999L);
    }

    @Test
    void createProduct_Success() {
        // given
        Product newProduct = new Product("New Product", 250.0);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });

        // when
        Product result = productService.createProduct(newProduct);

        // then
        assertNotNull(result);
        assertEquals(2L, result.getId());
        assertEquals("New Product", result.getName());
        assertEquals(250.0, result.getPrice());

        verify(productRepository).save(newProduct);
    }

    @Test
    void deleteProduct_Success() {
        // given
        doNothing().when(productRepository).deleteById(1L);

        // when
        productService.deleteProduct(1L);

        // then
        // brak wyjątków = sukces
        verify(productRepository).deleteById(1L);
    }
}
