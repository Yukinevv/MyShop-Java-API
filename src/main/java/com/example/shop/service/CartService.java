package com.example.shop.service;

import com.example.shop.entity.CartItem;
import com.example.shop.entity.Product;
import com.example.shop.entity.User;
import com.example.shop.repository.CartItemRepository;
import com.example.shop.repository.ProductRepository;
import com.example.shop.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // Czas (w minutach) rezerwacji – np. 15
    private static final int RESERVATION_MINUTES = 15;

    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Dodaje produkt do koszyka (rezerwacja).
     * - Waliduje, czy product istnieje
     * - Waliduje, czy user istnieje
     * - Ustawia expiresAt = now + 15 minut
     */
    @Transactional
    public CartItem addToCart(Long userId, Long productId, int quantity) {
        // Walidujemy usera
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono usera o ID: " + userId));

        // Walidujemy produkt
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono produktu o ID: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Brak wystarczającego stanu magazynowego dla produktu: "
                        + product.getName());
        }
        product.setStockQuantity(product.getStockQuantity() - quantity);
        productRepository.save(product);

        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setReservedAt(LocalDateTime.now());
        cartItem.setExpiresAt(LocalDateTime.now().plusMinutes(RESERVATION_MINUTES));

        return cartItemRepository.save(cartItem);
    }

    /**
     * Zwraca wszystkie pozycje koszyka danego użytkownika (nie wygasłe).
     */
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Nie znaleziono usera"));
        return cartItemRepository.findByUser(user);
    }

    /**
     * Usuwa konkretną pozycję z koszyka (np. user rezygnuje).
     */
    @Transactional
    public void removeFromCart(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    /**
     * Metoda cyklicznie wywoływana przez @Scheduled,
     * która usuwa wygasłe rezerwacje (where expiresAt < now).
     * Można też ewentualnie "oddawać" stock do Product, jeśli wcześniej odjęliśmy.
     */
    @Scheduled(fixedDelay = 5 * 60_000) // co 5 minut
    @Transactional
    public void cleanUpExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<CartItem> expiredList = cartItemRepository.findAllExpired(now);

        // Przywracamy stock dla wszystkich produktów z koszyka
         for (CartItem ci : expiredList) {
             Product product = ci.getProduct();
             product.setStockQuantity(product.getStockQuantity() + ci.getQuantity());
             productRepository.save(product);
         }

        // Następnie usuwamy z bazy
        for (CartItem ci : expiredList) {
            cartItemRepository.delete(ci);
        }
    }
}
