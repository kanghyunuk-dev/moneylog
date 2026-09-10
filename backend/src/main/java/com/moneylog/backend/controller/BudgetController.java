package com.moneylog.backend.controller;

import com.moneylog.backend.dto.request.BudgetCreateRequest;
import com.moneylog.backend.dto.request.BudgetUpdateRequest;
import com.moneylog.backend.dto.response.BudgetResponse;
import com.moneylog.backend.security.PrincipalDetails;
import com.moneylog.backend.service.BudgetService;
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
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@AuthenticationPrincipal PrincipalDetails principalDetails, @Valid @RequestBody BudgetCreateRequest request) {
        BudgetResponse response = budgetService.createBudget(principalDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgets(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        List<BudgetResponse> response = budgetService.getBudgets(principalDetails.getUser(), month.toString());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long id, @Valid @RequestBody BudgetUpdateRequest request) {
        BudgetResponse response = budgetService.updateBudget(principalDetails.getUser(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long id) {
        budgetService.deleteBudget(principalDetails.getUser(), id);
        return ResponseEntity.ok().build();
    }

}
