package com.moneylog.backend.dto.response;

public record CategorySpendingSummary(
        Long categoryId,
        Long totalAmount
) {}
