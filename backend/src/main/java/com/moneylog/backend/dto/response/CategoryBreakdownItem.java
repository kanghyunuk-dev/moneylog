package com.moneylog.backend.dto.response;

public record CategoryBreakdownItem(
        Long categoryId,
        String categoryName,
        Long amount,
        Double percentage
) {
}
