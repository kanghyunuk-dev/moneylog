package com.moneylog.backend.security;

import lombok.RequiredArgsConstructor;
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
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JWTTokenProvider jwtTokenProvider;

    private static final String KEY_PREFIX = "blacklist:";

    public void blacklist(String accessToken) {
        Date expiration = jwtTokenProvider.getExpirationFromToken(accessToken);
        long remainingMillis = expiration.getTime() - System.currentTimeMillis();

        if (remainingMillis <= 0) {
            return;
        }

        redisTemplate.opsForValue().set(KEY_PREFIX + hash(accessToken), "logout", Duration.ofMillis(remainingMillis));
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + hash(accessToken)));
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
