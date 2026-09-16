package com.moneylog.backend.service;

import com.moneylog.backend.dto.request.GoalCreateRequest;
import com.moneylog.backend.dto.request.GoalUpdateRequest;
import com.moneylog.backend.dto.response.GoalResponse;
import com.moneylog.backend.dto.response.TypeSpendingSummary;
import com.moneylog.backend.entity.CategoryType;
import com.moneylog.backend.entity.Goal;
import com.moneylog.backend.entity.GoalStatus;
import com.moneylog.backend.entity.User;
import com.moneylog.backend.exception.GoalAlreadyDoneException;
import com.moneylog.backend.exception.GoalNotFoundException;
import com.moneylog.backend.repository.GoalRepository;
import com.moneylog.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {
    private final GoalRepository goalRepository;
    private final TransactionRepository transactionRepository;

    @Transactional
    public GoalResponse createGoal(User user, GoalCreateRequest request) {
        boolean hasActive = goalRepository.findByUserAndStatus(user, GoalStatus.ACTIVE).isPresent();
        int nextOrder = goalRepository.findByUserOrderByDisplayOrderAsc(user).size();
        GoalStatus status = hasActive ? GoalStatus.WAITING : GoalStatus.ACTIVE;

        Goal goal = Goal.builder()
                .user(user)
                .name(request.name())
                .targetAmount(request.targetAmount())
                .currentAmount(0L)
                .deadline(request.deadline())
                .status(status)
                .activatedAt(status == GoalStatus.ACTIVE ? LocalDateTime.now() : null)
                .displayOrder(nextOrder)
                .build();

        return GoalResponse.from(goalRepository.save(goal));
    }

    @Transactional
    public List<GoalResponse> getGoals(User user) {
        List<Goal> goals = goalRepository.findByUserOrderByDisplayOrderAsc(user);

        goals.stream()
                .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
                .findFirst()
                .ifPresent(this::refreshProgress);

        return goals.stream()
                .map(GoalResponse::from)
                .toList();
    }

    @Transactional
    public GoalResponse updateGoal(User user, Long goalId, GoalUpdateRequest request) {
        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new GoalNotFoundException("존재하지 않는 목표 입니다"));

        if (goal.getStatus() == GoalStatus.DONE) {
            throw new GoalAlreadyDoneException("완료된 목표는 수정할 수 없습니다");
        }

        goal.update(request.name(), request.targetAmount(), request.deadline());

        return GoalResponse.from(goal);
    }


    @Transactional
    public void deleteGoal(User user, Long goalId) {
        Goal goal = goalRepository.findByIdAndUser(goalId, user)
                .orElseThrow(() -> new GoalNotFoundException("존재하지 않는 목표 입니다"));

        boolean wasActive = goal.getStatus() == GoalStatus.ACTIVE;
        goalRepository.delete(goal);

        if (wasActive) {
            promoteNextWaiting(user);
        }
    }

    private void refreshProgress(Goal goal) {
        LocalDate start = goal.getActivatedAt().toLocalDate();
        LocalDate end = LocalDate.now();

        Map<CategoryType, Long> sums = transactionRepository.sumAmountByTypeForPeriod(goal.getUser(), start, end).stream()
                .collect(Collectors.toMap(TypeSpendingSummary::type, TypeSpendingSummary::totalAmount));

        long income = sums.getOrDefault(CategoryType.INCOME, 0L);
        long expense = sums.getOrDefault(CategoryType.EXPENSE, 0L);
        long netSavings = income - expense;

        goal.setCurrentAmount(netSavings);

        if (goal.getCurrentAmount() >= goal.getTargetAmount()) {
            goal.complete();
            promoteNextWaiting(goal.getUser());
        }
    }

    private void promoteNextWaiting(User user) {
        goalRepository.findFirstByUserAndStatusOrderByDisplayOrderAsc(user, GoalStatus.WAITING)
                .ifPresent(next -> next.promote());
    }

}
