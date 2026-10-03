package com.moneylog.backend.dto.response;

public record MonthlyTrendItem(
        String month,
        Long income,
        Long expense
) {
}
