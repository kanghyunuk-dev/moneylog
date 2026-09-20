package com.moneylog.backend.security;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtils {

    private CookieUtils() {}

    public static ResponseCookie build(String name, String value, Duration maxAge) {
        return build(name, value, maxAge, "Strict");
    }

    public static ResponseCookie build(String name, String value, Duration maxAge, String sameSite) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
    }

}
