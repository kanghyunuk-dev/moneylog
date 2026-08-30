package com.moneylog.backend.security;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public class CookieUtils {

    private CookieUtils() {}

    public static ResponseCookie build(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }

}
