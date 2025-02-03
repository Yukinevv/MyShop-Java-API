package com.example.shop.controller;

import com.example.shop.entity.Product;
import com.example.shop.exception.GlobalExceptionHandler;
import com.example.shop.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testy kontrolera ProductController w trybie "standalone".
 */
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();

        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("Test Product");
        sampleProduct.setPrice(123.45);
        sampleProduct.setStockQuantity(10);
    }

    @Test
    void getAllProducts_Success() throws Exception {
        // given
        Product anotherProduct = new Product();
        anotherProduct.setId(2L);
        anotherProduct.setName("Second Product");
        anotherProduct.setPrice(999.99);
        anotherProduct.setStockQuantity(15);

        when(productService.getAllProducts()).thenReturn(List.of(sampleProduct, anotherProduct));

        // when + then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[0].stockQuantity").value(10))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].price").value(999.99))
                .andExpect(jsonPath("$[1].stockQuantity").value(15));

        verify(productService).getAllProducts();
    }

    @Test
    void getProductById_Success() throws Exception {
        // given
        when(productService.getProductById(1L)).thenReturn(sampleProduct);

        // when + then
        mockMvc.perform(get("/api/products/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(123.45))
                .andExpect(jsonPath("$.stockQuantity").value(10));

        verify(productService).getProductById(1L);
    }

    @Test
    void getProductById_NotFound() throws Exception {
        // given
        doThrow(new RuntimeException("Produkt nie istnieje"))
                .when(productService).getProductById(999L);

        mockMvc.perform(get("/api/products/{id}", 999L))
                .andExpect(status().isBadRequest());

        verify(productService).getProductById(999L);
    }

    @Test
    void createProduct_Success() throws Exception {
        // given
        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setPrice(200.0);
        newProduct.setStockQuantity(15);

        // Zakładamy, że serwis ustawia ID = 5
        Product savedProduct = new Product();
        savedProduct.setId(5L);
        savedProduct.setName("New Product");
        savedProduct.setPrice(200.0);
        savedProduct.setStockQuantity(15);

        when(productService.createProduct(any(Product.class))).thenReturn(savedProduct);

        // JSON input
//        String productJson = """
//            {
//              "name": "New Product",
//              "price": 200.0,
//              "stockQuantity": 15
//            }
//        """;

        // when + then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.price").value(200.0))
                .andExpect(jsonPath("$.stockQuantity").value(15));

        verify(productService).createProduct(any(Product.class));
    }

    @Test
    void deleteProduct_Success() throws Exception {
        // given
        doNothing().when(productService).deleteProduct(1L);

        // when + then
        mockMvc.perform(delete("/api/products/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(1L);
    }

    @Test
    void updateStockQuantity_Success() throws Exception {
        // given
        Product newProduct = new Product();
        newProduct.setName("New Product");
        newProduct.setPrice(200.0);
        newProduct.setStockQuantity(10);

        Product savedProduct = new Product();
        savedProduct.setId(2L);
        savedProduct.setName("New Product");
        savedProduct.setPrice(200.0);
        savedProduct.setStockQuantity(5);

        when(productService.updateStockQuantity(2L, 5)).thenReturn(savedProduct);

        // when + then
        mockMvc.perform(patch("/api/products/{id}/stock", savedProduct.getId())
                        .param("quantity", String.valueOf(5))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.price").value(200.0))
                .andExpect(jsonPath("$.stockQuantity").value(5));

        verify(productService).updateStockQuantity(2L, 5);
    }
}
