package com.daehanforeigner.capstone.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtProvider {

    private final SecretKey secretKey;

    private final long accessTokenExpMin;

    private final long refreshTokenExpDay;

    public JwtProvider(
            @Value("${custom.jwt.secret-key}") String secretKey,
            @Value("${custom.jwt.access-token-exp-min}") long accessTokenExpMin,
            @Value("${custom.jwt.refresh-token-exp-day}") long refreshTokenExpDay
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpMin = accessTokenExpMin * 60 * 1000;
        this.refreshTokenExpDay = refreshTokenExpDay * 24 * 60 * 60 * 1000;
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, accessTokenExpMin);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenExpDay);
    }

    public String createToken(Long userId, long expMillis) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String .valueOf(userId))
                .issuedAt(now)
                .expiration(new Date (now.getTime() + expMillis))
                .signWith(secretKey)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 토큰입니다. : {}", e.getMessage());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 토큰입니다. : {}", e.getMessage());
        }
        return false;
    }
}
