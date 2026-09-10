package com.moneylog.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BudgetUpdateRequest(
        @NotNull(message = "금액을 입력해주세요")
        @Positive(message = "금액은 0보다 커야 합니다")
        Long amount
) {}
