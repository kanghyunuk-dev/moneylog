package com.moneylog.backend.dto.response;

import com.moneylog.backend.entity.CategoryType;

public record TypeSpendingSummary(
        CategoryType type,
        Long totalAmount
) {}
