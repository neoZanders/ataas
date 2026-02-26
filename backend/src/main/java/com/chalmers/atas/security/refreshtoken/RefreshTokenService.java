package com.chalmers.atas.security.refreshtoken;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import com.chalmers.atas.common.TransactionalResult;
import com.chalmers.atas.domain.user.User;
import com.chalmers.atas.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-ttl-seconds:90000}")
    long refreshTokenTtlSeconds;

    @Transactional
    public Pair<User, String> issueRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(refreshTokenTtlSeconds);

        String rawToken = jwtService.generateRefreshToken(user.getUsername());
        String tokenHash = hashToken(rawToken);

        RefreshToken entity = RefreshToken.of(user, tokenHash, expiry, now);
        refreshTokenRepository.save(entity);

        return Pair.of(user, rawToken);
    }

    @Transactional
    public TransactionalResult<Pair<User, String>> rotateRefreshToken(String rawToken) {
        Result<Claims> jwtCheck = jwtService.parseRefreshClaims(rawToken);
        if (!jwtCheck.isSuccess()) {
            return TransactionalResult.rollbackFor(jwtCheck.getError());
        }

        String tokenHash = hashToken(rawToken);

        Optional<RefreshToken> maybeRefreshToken =
                refreshTokenRepository.findByTokenHashWithUser(tokenHash);

        if (maybeRefreshToken.isEmpty()) {
            return TransactionalResult.rollbackFor(ErrorCode.REFRESH_TOKEN_NOT_FOUND.toError());
        }

        RefreshToken current = maybeRefreshToken.get();

        if (current.isRevoked()) {
            return TransactionalResult.rollbackFor(ErrorCode.REFRESH_TOKEN_REVOKED.toError());
        }

        if (current.getExpiresAt().isBefore(Instant.now())) {
            return TransactionalResult.rollbackFor(ErrorCode.REFRESH_TOKEN_EXPIRED.toError());
        }

        current.setRevoked(true);
        current.setLastUsedAt(Instant.now());

        return TransactionalResult.ok(issueRefreshToken(current.getUser()));
    }

    @Transactional
    public void revokeToken(String rawToken) {
        String tokenHash = hashToken(rawToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(refreshToken -> refreshToken.setRevoked(true));
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
