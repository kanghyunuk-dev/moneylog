package com.moneylog.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record GoalCreateRequest(
        @NotNull(message = "목표명을 입력해주세요")
        @Size(min = 1, max = 50, message = "목표명은 50자 이하로 입력해주세요")
        String name,

        @NotNull(message = "목표 금액을 입력해주세요")
        @Positive(message = "목표 금액은 0보다 커야 합니다")
        Long targetAmount,

        LocalDate deadline
) {
}
