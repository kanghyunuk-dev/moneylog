package com.moneylog.backend.dto.response;

import com.moneylog.backend.entity.CategoryType;
import com.moneylog.backend.entity.Transaction;

import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        Long categoryId,
        String categoryName,
        CategoryType categoryType,
        String categoryIcon,
        Long amount,
        LocalDate transactionDate,
        String memo
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getCategory().getId(),
                transaction.getCategory().getName(),
                transaction.getCategory().getType(),
                transaction.getCategory().getIcon(),
                transaction.getAmount(),
                transaction.getTransactionDate(),
                transaction.getMemo()
        );
    }
}
