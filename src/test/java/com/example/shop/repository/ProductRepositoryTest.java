package com.example.shop.repository;

import com.example.shop.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy repozytorium ProductRepository,
 * z bazą testową MSSQL (lub inną).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        // Można wyczyścić repozytorium (w razie potrzeby):
        // productRepository.deleteAll();
    }

    @Test
    void saveProduct_GeneratesId() {
        // given
        Product product = new Product("Laptop Lenovo", 2999.99);

        // when
        Product saved = productRepository.save(product);

        // then
        assertNotNull(saved.getId(), "ID should be generated");
        assertEquals("Laptop Lenovo", saved.getName());
        assertEquals(2999.99, saved.getPrice());
    }

    @Test
    void findById_ReturnsProduct_WhenExists() {
        // given
        Product product = new Product("Smartphone", 1500.0);
        productRepository.save(product);

        // when
        Optional<Product> foundOpt = productRepository.findById(product.getId());

        // then
        assertTrue(foundOpt.isPresent());
        Product found = foundOpt.get();
        assertEquals("Smartphone", found.getName());
        assertEquals(1500.0, found.getPrice());
    }

    @Test
    void findById_ReturnsEmpty_WhenNotExists() {
        // given - no product with ID = 9999

        // when
        Optional<Product> foundOpt = productRepository.findById(9999L);

        // then
        assertTrue(foundOpt.isEmpty());
    }

    @Test
    void findAll_ReturnsListOfProducts() {
        // given
        Product product1 = new Product("Printer", 600.0);
        Product product2 = new Product("Headphones", 200.0);
        productRepository.save(product1);
        productRepository.save(product2);

        // when
        List<Product> allProducts = productRepository.findAll();

        // then
        assertEquals(2, allProducts.size());
    }

    @Test
    void deleteById_RemovesProduct() {
        // given
        Product product = new Product("Tablet", 999.99);
        productRepository.save(product);

        // when
        productRepository.deleteById(product.getId());

        // then
        Optional<Product> foundOpt = productRepository.findById(product.getId());
        assertTrue(foundOpt.isEmpty(), "Product should be removed from repo");
    }
}
