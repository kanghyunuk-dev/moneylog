package com.moneylog.backend.dto.response;

public record DashboardSummaryResponse(
        Long totalIncome,
        Long totalExpense,
        Long netAmount,
        Double savingsRate,
        Long incomeChange,
        Long expenseChange
) {
}
