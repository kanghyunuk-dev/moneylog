package com.moneylog.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record NicknameUpdateRequest(
        @NotBlank
        String nickname
) {}
