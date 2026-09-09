package com.moneylog.backend.service;

import com.moneylog.backend.dto.request.BudgetCreateRequest;
import com.moneylog.backend.dto.request.BudgetUpdateRequest;
import com.moneylog.backend.dto.response.BudgetResponse;
import com.moneylog.backend.entity.Budget;
import com.moneylog.backend.entity.Category;
import com.moneylog.backend.entity.User;
import com.moneylog.backend.exception.BudgetNotFoundException;
import com.moneylog.backend.exception.CategoryNotFoundException;
import com.moneylog.backend.exception.DuplicateBudgetException;
import com.moneylog.backend.repository.BudgetRepository;
import com.moneylog.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public BudgetResponse createBudget(User user, BudgetCreateRequest request) {
        if(budgetRepository.existsByUserAndCategoryIdAndBudgetMonth(user, request.categoryId(), request.budgetMonth().toString())) {
            throw new DuplicateBudgetException("이미 해당 월에 예산이 설정된 카테고리 입니다");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("존재하지 않는 카테고리 입니다"));

        Budget budget = Budget.builder()
                .user(user)
                .category(category)
                .budgetMonth(request.budgetMonth().toString())
                .amount(request.amount())
                .build();

        Budget saved = budgetRepository.save(budget);
        return BudgetResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<BudgetResponse> getBudgets(User user, String budgetMonth) {
        return budgetRepository.findByUserAndBudgetMonth(user, budgetMonth)
                .stream()
                .map(BudgetResponse::from)
                .toList();
    }

    @Transactional
    public BudgetResponse updateBudget(User user, Long budgetId, BudgetUpdateRequest request) {
        Budget budget = budgetRepository.findByIdAndUser(budgetId, user)
                .orElseThrow(() -> new BudgetNotFoundException("존재하지 않는 예산입니다"));

        budget.update(request.amount());

        return BudgetResponse.from(budget);
    }

    @Transactional
    public void deleteBudget(User user, Long budgetId) {
        Budget budget = budgetRepository.findByIdAndUser(budgetId, user)
                .orElseThrow(() -> new BudgetNotFoundException("존재하지 않는 예산입니다"));

        budgetRepository.delete(budget);
    }
}
