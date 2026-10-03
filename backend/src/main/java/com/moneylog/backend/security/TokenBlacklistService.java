package com.moneylog.backend.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Date;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JWTTokenProvider jwtTokenProvider;
    private static final String KEY_PREFIX = "blacklist:";

    public void blacklist(String accessToken) {
        Date expiration;
        try {
            expiration = jwtTokenProvider.getExpirationFromToken(accessToken);
        } catch (JwtException e) {
            log.warn("잘못된 토큰이라 블랙리스트 등록을 건너뜀: {}", e.toString());
            return;
        }

        long remainingMillis = expiration.getTime() - System.currentTimeMillis();
        if (remainingMillis <= 0) return;

        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + hash(accessToken), "logout", Duration.ofMillis(remainingMillis));
        } catch (DataAccessException e) {
            log.warn("Redis 장애로 블랙리스트 등록 실패, 로그아웃은 계속 진행: {}", e.toString());
        }
    }

    public boolean isBlacklisted(String accessToken) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + hash(accessToken)));
        } catch (DataAccessException e) {
            log.warn("Redis 장애로 블랙리스트 확인 불가, 통과 처리: {}", e.toString());
            return false;
        }
    }

    private String hash(String accessToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(accessToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("해시 알고리즘을 찾을 수 없습니다", e);
        }
    }

}
