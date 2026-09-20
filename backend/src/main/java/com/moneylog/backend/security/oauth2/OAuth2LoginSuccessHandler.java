package com.moneylog.backend.security.oauth2;

import com.moneylog.backend.dto.response.TokenResponse;
import com.moneylog.backend.security.AppProperties;
import com.moneylog.backend.security.CookieUtils;
import com.moneylog.backend.security.JWTProperties;
import com.moneylog.backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final JWTProperties jwtProperties;
    private final AppProperties appProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        Boolean emailVerified = oAuth2User.getAttribute("email_verified");
        if (emailVerified == null || !emailVerified) {
            response.sendRedirect(appProperties.getFrontendUrl() + "/login?error=email_not_verified");
            return;
        }

        String email = oAuth2User.getAttribute("email");
        String nickname = oAuth2User.getAttribute("name");
        String providerId = oAuth2User.getAttribute("sub");

        TokenResponse tokens = authService.loginWithGoogle(email, nickname, providerId);

        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.build("accessToken", tokens.accessToken(), Duration.ofMillis(jwtProperties.getAccessTokenExpiration())).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.build("refreshToken", tokens.refreshToken(), Duration.ofMillis(jwtProperties.getRefreshTokenExpiration())).toString());

        response.sendRedirect(appProperties.getFrontendUrl() + "/");
    }
}
