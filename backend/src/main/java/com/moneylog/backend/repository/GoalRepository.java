package com.moneylog.backend.repository;

import com.moneylog.backend.entity.Goal;
import com.moneylog.backend.entity.GoalStatus;
import com.moneylog.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUserOrderByDisplayOrderAsc(User user);

    Optional<Goal> findByIdAndUser(Long id, User user);

    Optional<Goal> findByUserAndStatus(User user, GoalStatus status);

    Optional<Goal> findFirstByUserAndStatusOrderByDisplayOrderAsc(User user, GoalStatus status);

}
