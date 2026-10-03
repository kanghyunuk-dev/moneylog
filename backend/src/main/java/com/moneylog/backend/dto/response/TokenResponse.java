package com.moneylog.backend.dto.response;

public record TokenResponse(
   String accessToken,
   String refreshToken
) {}
