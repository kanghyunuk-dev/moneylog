package com.moneylog.backend.security.oauth2;

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import java.util.Map;
import java.util.Set;

public record OAuth2AuthorizationRequestSnapshot(
        String authorizationUri,
        String clientId,
        String redirectUri,
        Set<String> scopes,
        String state,
        Map<String, Object> additionalParameters,
        String authorizationRequestUri,
        Map<String, Object> attributes
) {
    public static OAuth2AuthorizationRequestSnapshot from(OAuth2AuthorizationRequest authorizationRequest) {
        return new OAuth2AuthorizationRequestSnapshot(
                authorizationRequest.getAuthorizationUri(),
                authorizationRequest.getClientId(),
                authorizationRequest.getRedirectUri(),
                authorizationRequest.getScopes(),
                authorizationRequest.getState(),
                authorizationRequest.getAdditionalParameters(),
                authorizationRequest.getAuthorizationRequestUri(),
                authorizationRequest.getAttributes()
        );
    }

    public OAuth2AuthorizationRequest toAuthorizationRequest() {
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(authorizationUri)
                .clientId(clientId)
                .redirectUri(redirectUri)
                .scopes(scopes)
                .state(state)
                .additionalParameters(additionalParameters)
                .authorizationRequestUri(authorizationRequestUri)
                .attributes(attributes)
                .build();
    }
}
