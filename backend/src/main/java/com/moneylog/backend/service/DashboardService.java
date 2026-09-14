package com.moneylog.backend.service;

import com.moneylog.backend.dto.response.*;
import com.moneylog.backend.entity.Category;
import com.moneylog.backend.entity.CategoryType;
import com.moneylog.backend.entity.Transaction;
import com.moneylog.backend.entity.User;
import com.moneylog.backend.repository.CategoryRepository;
import com.moneylog.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(User user, YearMonth month) {
        Map<CategoryType, Long> thisMonth = sumByType(user, month);
        Map<CategoryType, Long> lastMonth = sumByType(user, month.minusMonths(1));

        long totalIncome = thisMonth.getOrDefault(CategoryType.INCOME, 0L);
        long totalExpense = thisMonth.getOrDefault(CategoryType.EXPENSE, 0L);
        long netAmount = totalIncome - totalExpense;
        double savingsRate = totalIncome == 0 ? 0 : (double) netAmount / totalIncome * 100;
        savingsRate = Math.max(-100, Math.min(100, savingsRate));

        long lastIncome = lastMonth.getOrDefault(CategoryType.INCOME, 0L);
        long lastExpense = lastMonth.getOrDefault(CategoryType.EXPENSE, 0L);

        return new DashboardSummaryResponse(
                totalIncome,
                totalExpense,
                netAmount,
                savingsRate,
                totalIncome - lastIncome,
                totalExpense - lastExpense
        );
    }

    @Transactional(readOnly = true)
    public List<CategoryBreakdownItem> getCategoryBreakdown(User user, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<CategorySpendingSummary> summaries = transactionRepository.sumExpenseByCategoryForMonth(user, start, end);
        long total = summaries.stream().mapToLong(CategorySpendingSummary::totalAmount).sum();

        List<Long> categoryIds = summaries.stream().map(CategorySpendingSummary::categoryId).toList();
        Map<Long, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, c -> c));

        return summaries.stream()
                .map(s -> {
                    Category category = categoryMap.get(s.categoryId());
                    double percentage = total == 0 ? 0 : (double) s.totalAmount() / total * 100;
                    return new CategoryBreakdownItem(s.categoryId(), category.getName(), s.totalAmount(), percentage);
                })
                .sorted((a, b) -> Long.compare(b.amount(), a.amount()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MonthlyTrendItem> getMonthlyTrend(User user, YearMonth baseMonth, int months) {
        YearMonth end = baseMonth;
        YearMonth start = end.minusMonths(months - 1);

        List<Transaction> transactions = transactionRepository.findAllForTrend(user, start.atDay(1), end.atEndOfMonth());

        Map<YearMonth, List<Transaction>> byMonth = transactions.stream()
                .collect(Collectors.groupingBy(t -> YearMonth.from(t.getTransactionDate())));

        List<MonthlyTrendItem> result = new ArrayList<>();
        for (YearMonth m = start; !m.isAfter(end); m = m.plusMonths(1)) {
            List<Transaction> monthTransactions = byMonth.getOrDefault(m, List.of());
            long income = monthTransactions.stream().filter(t -> t.getCategory().getType() == CategoryType.INCOME).mapToLong(Transaction::getAmount).sum();
            long expense = monthTransactions.stream().filter(t -> t.getCategory().getType() == CategoryType.EXPENSE).mapToLong(Transaction::getAmount).sum();
            result.add(new MonthlyTrendItem(m.toString(), income, expense));
        }
        return result;
    }

    private Map<CategoryType, Long> sumByType(User user, YearMonth month) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return transactionRepository.sumAmountByTypeForMonth(user, start, end).stream()
                .collect(Collectors.toMap(TypeSpendingSummary::type, TypeSpendingSummary::totalAmount));
    }
}
