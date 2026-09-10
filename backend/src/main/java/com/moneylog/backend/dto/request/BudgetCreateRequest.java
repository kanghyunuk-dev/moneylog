package com.moneylog.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.YearMonth;

public record BudgetCreateRequest(
        @NotNull(message = "카테고리를 선택해주세요")
        Long categoryId,

        @NotNull(message = "예산 월을 선택해주세요")
        YearMonth budgetMonth,

        @NotNull(message = "금액을 입력해주세요")
        @Positive(message = "금액은 0보다 커야 합니다")
        Long amount
) {}
