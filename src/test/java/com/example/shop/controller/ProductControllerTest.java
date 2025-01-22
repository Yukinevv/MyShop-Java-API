package com.example.shop.controller;

import com.example.shop.entity.Product;
import com.example.shop.exception.GlobalExceptionHandler;
import com.example.shop.service.ProductService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        sampleProduct = new Product();
        sampleProduct.setId(1L);
        sampleProduct.setName("Test Product");
        sampleProduct.setPrice(123.45);
    }

    @Test
    void getAllProducts_Success() throws Exception {
        // given
        Product anotherProduct = new Product();
        anotherProduct.setId(2L);
        anotherProduct.setName("Second Product");
        anotherProduct.setPrice(999.99);

        when(productService.getAllProducts()).thenReturn(List.of(sampleProduct, anotherProduct));

        // when + then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Product"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].price").value(999.99));

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
                .andExpect(jsonPath("$.price").value(123.45));

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

        // Zakładamy, że serwis ustawia ID = 5
        Product savedProduct = new Product();
        savedProduct.setId(5L);
        savedProduct.setName("New Product");
        savedProduct.setPrice(200.0);

        when(productService.createProduct(any(Product.class))).thenReturn(savedProduct);

        // JSON input
        String productJson = """
            {
              "name": "New Product",
              "price": 200.0
            }
        """;

        // when + then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("New Product"))
                .andExpect(jsonPath("$.price").value(200.0));

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
}
