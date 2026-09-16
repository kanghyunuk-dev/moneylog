package com.moneylog.backend.dto.response;

import com.moneylog.backend.entity.Goal;
import com.moneylog.backend.entity.GoalStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GoalResponse(
        Long id,
        String name,
        Long targetAmount,
        Long currentAmount,
        LocalDate deadline,
        GoalStatus status,
        Integer displayOrder,
        LocalDateTime createdAt,
        LocalDateTime activatedAt
) {
    public static GoalResponse from(Goal goal) {
        return new GoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getDeadline(),
                goal.getStatus(),
                goal.getDisplayOrder(),
                goal.getCreatedAt(),
                goal.getActivatedAt()
        );
    }
}
