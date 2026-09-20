package com.moneylog.backend.security.oauth2;

import com.moneylog.backend.security.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Base64;

@Component
public class CookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    public static final String COOKIE_NAME = "oauth2_auth_request";
    private static final Duration COOKIE_EXPIRE = Duration.ofSeconds(180);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(COOKIE_NAME)) {
                return deserialize(cookie.getValue());
            }
        }
        return null;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            deleteCookie(response);
            return;
        }

        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.build(COOKIE_NAME, serialize(authorizationRequest), COOKIE_EXPIRE, "Lax").toString());
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        deleteCookie(response);
        return authorizationRequest;
    }

    private void deleteCookie(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.build(COOKIE_NAME, "", Duration.ZERO, "Lax").toString());
    }

    private String serialize(OAuth2AuthorizationRequest authorizationRequest) {
        OAuth2AuthorizationRequestSnapshot snapshot = OAuth2AuthorizationRequestSnapshot.from(authorizationRequest);
        byte[] json = objectMapper.writeValueAsBytes(snapshot);
        return Base64.getUrlEncoder().encodeToString(json);
    }

    private OAuth2AuthorizationRequest deserialize(String value) {
        byte[] json = Base64.getUrlDecoder().decode(value);
        OAuth2AuthorizationRequestSnapshot snapshot = objectMapper.readValue(json, OAuth2AuthorizationRequestSnapshot.class);
        return snapshot.toAuthorizationRequest();
    }
}
