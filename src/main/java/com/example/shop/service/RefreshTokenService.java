package com.example.shop.service;

import com.example.shop.entity.RefreshToken;
import com.example.shop.entity.User;
import com.example.shop.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenDurationMs; // np. 604800000 (7 dni w ms)

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Tworzy nowy refresh token dla danego użytkownika i zapisuje w bazie.
     */
    public RefreshToken createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        // Generujemy losowy ciąg znaków. Może to być też JWT, wtedy musiałbyś go podpisać kluczem
        refreshToken.setToken(UUID.randomUUID().toString());

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Sprawdza, czy token jest w bazie i czy nie jest przeterminowany.
     */
    public Optional<RefreshToken> validateRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        if (refreshTokenOpt.isEmpty()) {
            return Optional.empty();
        }
        RefreshToken refreshToken = refreshTokenOpt.get();
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            // usuwamy z bazy, bo przeterminowany
            refreshTokenRepository.delete(refreshToken);
            return Optional.empty();
        }
        return Optional.of(refreshToken);
    }

    /**
     * Usuwa token z bazy (np. przy wylogowaniu).
     */
    public void deleteRefreshToken(String token) {
        refreshTokenRepository.deleteByToken(token);
    }

    /**
     * Usuwa wszystkie refresh tokeny danego użytkownika (opcjonalnie).
     */
    public void deleteAllForUser(User user) {
        var tokens = refreshTokenRepository.findAllByUser(user);
        refreshTokenRepository.deleteAll(tokens);
    }
}
