package com.example.shop.service;

import com.example.shop.entity.RefreshToken;
import com.example.shop.entity.User;
import com.example.shop.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Przykładowy użytkownik
        testUser = new User();
        testUser.setId(42L);
        testUser.setUsername("testuser");
        // wstrzyknięcie wartości refreshTokenDurationMs
        // (jeśli normalnie pobierana z application.properties @Value("${jwt.refresh.expiration}")
        // w teście robimy np.:
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", 86400000L); // 24h
    }

    @Test
    void createRefreshToken_Success() {
        // given
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> {
            RefreshToken tokenArg = inv.getArgument(0);
            tokenArg.setId(100L);
            return tokenArg;
        });

        // when
        RefreshToken created = refreshTokenService.createRefreshToken(testUser);

        // then
        assertNotNull(created);
        assertEquals(100L, created.getId());
        assertEquals(42L, created.getUser().getId());
        assertNotNull(created.getToken());
        assertFalse(created.getToken().isBlank());
        // sprawdzamy, czy data wygaśnięcia jest w przyszłości
        assertTrue(created.getExpiryDate().isAfter(Instant.now()));

        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void validateRefreshToken_Valid() {
        // given
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken validToken = new RefreshToken();
        validToken.setToken(tokenValue);
        validToken.setExpiryDate(Instant.now().plusSeconds(3600));
        validToken.setUser(testUser);

        when(refreshTokenRepository.findByToken(tokenValue))
                .thenReturn(Optional.of(validToken));

        // when
        Optional<RefreshToken> result = refreshTokenService.validateRefreshToken(tokenValue);

        // then
        assertTrue(result.isPresent());
        assertEquals(tokenValue, result.get().getToken());
        assertEquals(42L, result.get().getUser().getId());

        verify(refreshTokenRepository).findByToken(tokenValue);
        // nie usuwamy tokenu, bo jest wciąż ważny
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void validateRefreshToken_NotFound() {
        // given
        String tokenValue = "non-existent";
        when(refreshTokenRepository.findByToken(tokenValue))
                .thenReturn(Optional.empty());

        // when
        Optional<RefreshToken> result = refreshTokenService.validateRefreshToken(tokenValue);

        // then
        assertTrue(result.isEmpty());
        verify(refreshTokenRepository).findByToken(tokenValue);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void validateRefreshToken_Expired() {
        // given
        String tokenValue = UUID.randomUUID().toString();
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken(tokenValue);
        expiredToken.setExpiryDate(Instant.now().minusSeconds(10)); // wygasł 10s temu
        expiredToken.setUser(testUser);

        when(refreshTokenRepository.findByToken(tokenValue))
                .thenReturn(Optional.of(expiredToken));

        // when
        Optional<RefreshToken> result = refreshTokenService.validateRefreshToken(tokenValue);

        // then
        assertTrue(result.isEmpty());
        // bo token wygasł, serwis usuwa go i zwraca Optional.empty()

        verify(refreshTokenRepository).findByToken(tokenValue);
        // sprawdź, czy usuwa wygasły token
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    void deleteRefreshToken_Success() {
        // given
        String tokenValue = "some-token";
        doNothing().when(refreshTokenRepository).deleteByToken(tokenValue);

        // when
        refreshTokenService.deleteRefreshToken(tokenValue);

        // then
        verify(refreshTokenRepository).deleteByToken(tokenValue);
    }

    @Test
    void deleteAllForUser_Success() {
        // given
        RefreshToken token1 = new RefreshToken();
        token1.setId(1L);
        token1.setUser(testUser);

        RefreshToken token2 = new RefreshToken();
        token2.setId(2L);
        token2.setUser(testUser);

        when(refreshTokenRepository.findAllByUser(testUser))
                .thenReturn(List.of(token1, token2));

        // when
        refreshTokenService.deleteAllForUser(testUser);

        // then
        verify(refreshTokenRepository).findAllByUser(testUser);
        verify(refreshTokenRepository).deleteAll(anyList());
    }
}
