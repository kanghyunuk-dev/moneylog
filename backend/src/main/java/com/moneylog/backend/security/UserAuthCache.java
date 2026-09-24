package com.moneylog.backend.security;

public record UserAuthCache(Long userId, boolean deleted) {
    public static final String KEY_PREFIX = "user_auth:";
}
