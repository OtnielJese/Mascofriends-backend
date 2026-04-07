package com.vetivet.service;

import com.vetivet.model.RefreshToken;
import com.vetivet.model.User;
import com.vetivet.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${app.refresh-token.expiration:604800000}") // 7 days default
    private long refreshTokenExpiration;

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Revoke old tokens for this user to limit concurrent sessions
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Generated refresh token for user: {}", user.getEmail());
        return saved;
    }

    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .filter(rt -> !rt.isExpired())
                .filter(rt -> !rt.isRevoked())
                .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido o expirado"));
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
            log.info("Revoked refresh token for user: {}", rt.getUser().getEmail());
        });
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredAndRevokedTokens();
        log.info("Cleaned up expired and revoked refresh tokens");
    }
}
