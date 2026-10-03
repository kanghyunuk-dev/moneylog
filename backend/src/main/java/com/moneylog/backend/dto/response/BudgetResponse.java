package com.moneylog.backend.dto.response;

import com.moneylog.backend.entity.Budget;
import com.moneylog.backend.entity.CategoryType;

public record BudgetResponse(
        Long id,
        Long categoryId,
        String categoryName,
        CategoryType categoryType,
        String categoryIcon,
        String budgetMonth,
        Long amount,
        Long spentAmount
) {
    public static BudgetResponse from(Budget budget, Long spentAmount) {
        return new BudgetResponse(
                budget.getId(),
                budget.getCategory().getId(),
                budget.getCategory().getName(),
                budget.getCategory().getType(),
                budget.getCategory().getIcon(),
                budget.getBudgetMonth(),
                budget.getAmount(),
                spentAmount
        );
    }
}
