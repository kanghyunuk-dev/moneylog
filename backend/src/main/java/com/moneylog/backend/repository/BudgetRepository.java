package com.moneylog.backend.repository;

import com.moneylog.backend.entity.Budget;
import com.moneylog.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserAndBudgetMonth(User user, String budgetMonth);

    Optional<Budget> findByIdAndUser(Long id, User user);

    boolean existsByUserAndCategoryIdAndBudgetMonth(User user, Long categoryId, String budgetMonth);

}
