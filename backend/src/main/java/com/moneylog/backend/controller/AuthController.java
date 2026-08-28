package com.moneylog.backend.controller;

import com.moneylog.backend.dto.request.LoginRequest;
import com.moneylog.backend.dto.request.RefreshTokenRequest;
import com.moneylog.backend.dto.request.RegisterRequest;
import com.moneylog.backend.dto.response.ErrorResponse;
import com.moneylog.backend.dto.response.TokenResponse;
import com.moneylog.backend.dto.response.UserResponse;
import com.moneylog.backend.exception.InvalidTokenException;
import com.moneylog.backend.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        TokenResponse tokens = authService.login(request);

        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("accessToken", tokens.accessToken(), Duration.ofMinutes(30)).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("refreshToken", tokens.refreshToken(), Duration.ofDays(7)).toString());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if(refreshToken == null) {
            throw new InvalidTokenException("존재하지 않는 토큰입니다");
        }

        TokenResponse tokens = authService.refresh(new RefreshTokenRequest(refreshToken));

        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("accessToken", tokens.accessToken(), Duration.ofMinutes(30)).toString());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("인증이 필요합니다", "TOKEN_INVALID"));
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        if(refreshToken != null) {
            authService.logout(refreshToken);
        }

        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("accessToken", "", Duration.ZERO).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildCookie("refreshToken", "", Duration.ZERO).toString());

        return ResponseEntity.ok().build();
    }

    private ResponseCookie buildCookie(String name, String value, Duration maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
