package com.moneylog.backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public record WithdrawRequest(
        @NotBlank
        String password
) {}
