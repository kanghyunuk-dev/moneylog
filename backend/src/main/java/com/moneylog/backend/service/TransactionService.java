package com.moneylog.backend.service;

import com.moneylog.backend.dto.request.TransactionRequest;
import com.moneylog.backend.dto.response.TransactionResponse;
import com.moneylog.backend.entity.Category;
import com.moneylog.backend.entity.Transaction;
import com.moneylog.backend.entity.User;
import com.moneylog.backend.exception.CategoryNotFoundException;
import com.moneylog.backend.exception.TransactionNotFoundException;
import com.moneylog.backend.repository.CategoryRepository;
import com.moneylog.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public TransactionResponse createTransaction(User user, TransactionRequest request) {
        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("존재하지 않는 카테고리 입니다"));

        Transaction transaction = Transaction.builder()
                .user(user)
                .category(category)
                .amount(request.amount())
                .transactionDate(request.transactionDate())
                .memo(request.memo())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return TransactionResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactions(User user, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        return transactionRepository
                .findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(user, start, end)
                .stream()
                .map(TransactionResponse::from)
                .toList();
    }

    @Transactional
    public TransactionResponse updateTransaction(User user, Long transactionId, TransactionRequest request) {
        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new TransactionNotFoundException("존재하지 않는 거래 입니다"));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("존재하지 않는 카테고리 입니다"));

        transaction.update(category, request.amount(), request.transactionDate(), request.memo());

        return TransactionResponse.from(transaction);
    }

    @Transactional
    public void deleteTransaction(User user, Long transactionId) {
        Transaction transaction = transactionRepository.findByIdAndUser(transactionId, user)
                .orElseThrow(() -> new TransactionNotFoundException("존재하지 않는 거래 입니다"));

        transactionRepository.delete(transaction);
    }
}
