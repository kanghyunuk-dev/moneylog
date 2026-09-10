package com.moneylog.backend.repository;

import com.moneylog.backend.dto.response.CategorySpendingSummary;
import com.moneylog.backend.entity.Transaction;
import com.moneylog.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("SELECT t FROM Transaction t JOIN FETCH t.category WHERE t.user = :user AND t.transactionDate BETWEEN :start AND :end ORDER BY t.transactionDate DESC, t.id DESC")
    List<Transaction> findByUserAndTransactionDateBetweenOrderByTransactionDateDescIdDesc(@Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end);

    Optional<Transaction> findByIdAndUser(Long id, User user);

    @Query("SELECT new com.moneylog.backend.dto.response.CategorySpendingSummary(t.category.id, SUM(t.amount)) " +
            "FROM Transaction t WHERE t.user = :user AND t.transactionDate BETWEEN :start AND :end " +
            "GROUP BY t.category.id")
    List<CategorySpendingSummary> sumAmountByCategoryForMonth(@Param("user") User user, @Param("start") LocalDate start, @Param("end") LocalDate end);

}
