package com.chalmers.atas.security;

import com.chalmers.atas.common.ErrorCode;
import com.chalmers.atas.common.Result;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    private final Key signingKey;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-ttl-seconds:900}") long accessTokenTtlSeconds,
            @Value("${app.jwt.refresh-ttl-seconds:90000}") long refreshTokenTtlSeconds
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = Duration.ofSeconds(accessTokenTtlSeconds);
        this.refreshTokenTtl = Duration.ofSeconds(refreshTokenTtlSeconds);
    }

    public String generateAccessToken(String subject, Map<String, Object> extraClaims) {
        Map<String, Object> claims = new HashMap<>(extraClaims);
        claims.put("tokenType", "access");
        return buildToken(subject, claims, accessTokenTtl);
    }

    public String generateRefreshToken(String subject) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        return buildToken(subject, claims, refreshTokenTtl);
    }

    public String extractSubject(String token) {
        return parse(token).getPayload().getSubject();
    }

    public boolean isTokenValid(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isAccessToken(String token) {
        try {
            Claims claims = parse(token).getPayload();
            return "access".equals(claims.get("tokenType", String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public Result<Claims> parseRefreshClaims(String token) {
        try {
            Claims claims = parse(token).getPayload();
            String tokenType = claims.get("tokenType", String.class);

            if (!"refresh".equals(tokenType)) {
                return Result.error(ErrorCode.INVALID_REFRESH_TOKEN.toError("Wrong token type"));
            }

            return Result.ok(claims);
        } catch (ExpiredJwtException e) {
            return Result.error(ErrorCode.REFRESH_TOKEN_EXPIRED.toError());
        } catch (JwtException | IllegalArgumentException e) {
            return Result.error(ErrorCode.INVALID_REFRESH_TOKEN.toError());
        }
    }

    private String buildToken(String subject, Map<String, Object> claims, Duration tokenTtl) {
        Instant now = Instant.now();
        Instant expiry = now.plus(tokenTtl);

        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    private Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) signingKey)
                .build()
                .parseSignedClaims(token);
    }
}
