package com.moneylog.backend.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TransactionRequest(

    @NotNull
    Long categoryId,

    @NotNull
    @Positive
    Long amount,

    @NotNull
    LocalDate transactionDate,

    @Size(max = 200)
    String memo

) {}
