package com.moneylog.backend.controller;

import com.moneylog.backend.dto.request.TransactionRequest;
import com.moneylog.backend.dto.response.TransactionResponse;
import com.moneylog.backend.security.PrincipalDetails;
import com.moneylog.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@AuthenticationPrincipal PrincipalDetails principalDetails, @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(principalDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        List<TransactionResponse> response = transactionService.getTransactions(principalDetails.getUser(), month);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.updateTransaction(principalDetails.getUser(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long id) {
        transactionService.deleteTransaction(principalDetails.getUser(), id);
        return ResponseEntity.ok().build();
    }

}
