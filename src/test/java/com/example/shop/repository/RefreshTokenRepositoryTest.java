package com.example.shop.repository;

import com.example.shop.entity.RefreshToken;
import com.example.shop.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy repozytorium RefreshTokenRepository z prawdziwą bazą (MSSQL) w profilu "test".
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles("test")
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Tworzymy w bazie użytkownika, do którego będą przypisywane refresh tokens
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPass");
        testUser.setRole("ROLE_USER");
        userRepository.save(testUser);
    }

    @Test
    void saveRefreshToken_GeneratesId() {
        // given
        RefreshToken token = new RefreshToken();
        token.setToken("abcd1234");
        token.setUser(testUser);
        token.setExpiryDate(Instant.now().plusSeconds(3600));

        // when
        RefreshToken saved = refreshTokenRepository.save(token);

        // then
        assertNotNull(saved.getId(), "ID powinno być wygenerowane");
        assertEquals("abcd1234", saved.getToken());
        assertEquals(testUser.getId(), saved.getUser().getId());
        assertNotNull(saved.getExpiryDate());
    }

    @Test
    void findByToken_ReturnsRefreshToken_WhenExists() {
        // given
        RefreshToken token = new RefreshToken();
        token.setToken("xyz123");
        token.setUser(testUser);
        token.setExpiryDate(Instant.now().plusSeconds(600));
        refreshTokenRepository.save(token);

        // when
        Optional<RefreshToken> foundOpt = refreshTokenRepository.findByToken("xyz123");

        // then
        assertTrue(foundOpt.isPresent());
        RefreshToken found = foundOpt.get();
        assertEquals("xyz123", found.getToken());
        assertEquals(testUser.getId(), found.getUser().getId());
    }

    @Test
    void findByToken_ReturnsEmpty_WhenNotExists() {
        // given - brak zapisanego tokena "noSuchToken"

        // when
        Optional<RefreshToken> foundOpt = refreshTokenRepository.findByToken("noSuchToken");

        // then
        assertTrue(foundOpt.isEmpty(), "Powinno zwrócić Optional.empty() gdy token nie istnieje");
    }

    @Test
    void findAllByUser_ReturnsMultipleTokens() {
        // given
        RefreshToken token1 = new RefreshToken();
        token1.setToken("tokenA");
        token1.setUser(testUser);
        token1.setExpiryDate(Instant.now().plusSeconds(1000));
        refreshTokenRepository.save(token1);

        RefreshToken token2 = new RefreshToken();
        token2.setToken("tokenB");
        token2.setUser(testUser);
        token2.setExpiryDate(Instant.now().plusSeconds(2000));
        refreshTokenRepository.save(token2);

        // when
        List<RefreshToken> allForUser = refreshTokenRepository.findAllByUser(testUser);

        // then
        assertEquals(2, allForUser.size());
        assertTrue(allForUser.stream().anyMatch(rt -> rt.getToken().equals("tokenA")));
        assertTrue(allForUser.stream().anyMatch(rt -> rt.getToken().equals("tokenB")));
    }

    @Test
    void deleteByToken_RemovesToken() {
        // given
        RefreshToken token = new RefreshToken();
        token.setToken("deleteMe");
        token.setUser(testUser);
        token.setExpiryDate(Instant.now().plusSeconds(3600));
        refreshTokenRepository.save(token);

        // when
        refreshTokenRepository.deleteByToken("deleteMe");

        // then
        Optional<RefreshToken> foundOpt = refreshTokenRepository.findByToken("deleteMe");
        assertTrue(foundOpt.isEmpty(), "Token powinien zostać usunięty");
    }

    @Test
    void uniqueToken_ThrowsException_IfDuplicate() {
        // given
        RefreshToken token1 = new RefreshToken();
        token1.setToken("duplicateToken");
        token1.setUser(testUser);
        token1.setExpiryDate(Instant.now().plusSeconds(1000));
        refreshTokenRepository.save(token1);

        // token o tej samej wartości:
        RefreshToken token2 = new RefreshToken();
        token2.setToken("duplicateToken");
        token2.setUser(testUser);
        token2.setExpiryDate(Instant.now().plusSeconds(2000));

        // when + then
        // W zależności od dialektu i bazy (MSSQL) może to być DataIntegrityViolationException
        // lub inny (ConstraintViolationException)
        assertThrows(Exception.class, () -> {
            refreshTokenRepository.saveAndFlush(token2);
        });
    }
}
