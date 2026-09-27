package com.moneylog.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CookieUtils {

    private final AppProperties appProperties;

    public ResponseCookie build(String name, String value, Duration maxAge) {
        return build(name, value, maxAge, "Strict");
    }

    public ResponseCookie build(String name, String value, Duration maxAge, String sameSite) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(appProperties.isCookieSecure())
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
    }

}
