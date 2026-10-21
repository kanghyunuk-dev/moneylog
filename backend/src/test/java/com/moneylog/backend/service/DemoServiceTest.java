package com.moneylog.backend.service;

import com.moneylog.backend.dto.response.GoalResponse;
import com.moneylog.backend.entity.GoalStatus;
import com.moneylog.backend.entity.Transaction;
import com.moneylog.backend.entity.User;
import com.moneylog.backend.exception.DemoLimitExceededException;
import com.moneylog.backend.repository.BudgetRepository;
import com.moneylog.backend.repository.TransactionRepository;
import com.moneylog.backend.repository.UserRepository;
import com.moneylog.backend.scheduler.UserCleanupScheduler;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 체험 계정 생성·정리 통합 테스트 (테스트 후 롤백)
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class DemoServiceTest {

    private final DemoService demoService;
    private final GoalService goalService;
    private final UserCleanupScheduler userCleanupScheduler;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final EntityManager entityManager;

    @Test
    void 체험_계정을_만들면_오늘까지의_샘플_데이터가_함께_저장된다() {
        // when
        User user = demoService.createDemoUser();

        // then
        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);
        List<Transaction> transactions = transactionRepository.findAllForTrend(user, thisMonth.minusMonths(5).atDay(1), thisMonth.atEndOfMonth());

        assertThat(user.getProvider()).isEqualTo(DemoService.PROVIDER);
        assertThat(transactions).isNotEmpty();
        assertThat(transactions).allMatch(t -> !t.getTransactionDate().isAfter(today));
        assertThat(transactions).anyMatch(t -> YearMonth.from(t.getTransactionDate()).equals(thisMonth.minusMonths(5)));
        assertThat(budgetRepository.findByUserAndBudgetMonth(user, thisMonth.toString())).hasSize(4);
    }

    @Test
    void 진행_중_목표는_조회해도_자동_완료되지_않고_진행률이_표시된다() {
        // given
        User user = demoService.createDemoUser();

        // when - 조회 시 진행률 재계산
        List<GoalResponse> goals = goalService.getGoals(user);

        // then
        GoalResponse active = goals.stream()
                .filter(g -> g.status() == GoalStatus.ACTIVE)
                .findFirst()
                .orElseThrow();

        assertThat(goals).hasSize(4);
        assertThat(active.currentAmount()).isPositive().isLessThan(active.targetAmount());
        assertThat(goals).filteredOn(g -> g.status() == GoalStatus.WAITING).hasSize(2);
        assertThat(goals).filteredOn(g -> g.status() == GoalStatus.DONE).hasSize(1);
    }

    @Test
    void 최근_1시간_체험_계정이_한도에_도달하면_더_만들지_않는다() {
        // given - 한도만큼 체험 계정 생성 (샘플 데이터 없이)
        for (int i = 0; i < DemoService.HOURLY_LIMIT; i++) {
            userRepository.save(User.builder()
                    .email("demo-limit-" + UUID.randomUUID() + "@moneylog.demo")
                    .nickname("체험 사용자")
                    .provider(DemoService.PROVIDER)
                    .build());
        }

        // when & then
        assertThatThrownBy(() -> demoService.createDemoUser())
                .isInstanceOf(DemoLimitExceededException.class);
    }

    @Test
    void 만든_지_24시간_지난_체험_계정은_데이터와_함께_삭제된다() {
        // given
        User user = demoService.createDemoUser();
        setCreatedAt(user, LocalDateTime.now().minusHours(25));

        // when
        userCleanupScheduler.cleanupDemoUsers();
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(userRepository.findById(user.getId())).isEmpty();
        assertThat(countByUser("transaction", user)).isZero();
        assertThat(countByUser("budget", user)).isZero();
        assertThat(countByUser("goal", user)).isZero();
    }

    @Test
    void 만든_지_24시간이_안_된_체험_계정과_일반_회원은_삭제되지_않는다() {
        // given
        User recentDemoUser = demoService.createDemoUser();
        User oldLocalUser = userRepository.save(User.builder()
                .email("local-" + UUID.randomUUID() + "@test.com")
                .password("encoded-password")
                .nickname("일반회원")
                .provider("LOCAL")
                .build());
        setCreatedAt(oldLocalUser, LocalDateTime.now().minusDays(30));

        // when
        userCleanupScheduler.cleanupDemoUsers();
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(userRepository.findById(recentDemoUser.getId())).isPresent();
        assertThat(userRepository.findById(oldLocalUser.getId())).isPresent();
    }

    // created_at은 @PrePersist가 현재 시각으로 채우므로 직접 UPDATE
    private void setCreatedAt(User user, LocalDateTime createdAt) {
        entityManager.flush();
        entityManager.createNativeQuery("UPDATE users SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", user.getId())
                .executeUpdate();
        entityManager.clear();
    }

    private long countByUser(String table, User user) {
        Object count = entityManager.createNativeQuery("SELECT COUNT(*) FROM " + table + " WHERE user_id = :userId")
                .setParameter("userId", user.getId())
                .getSingleResult();
        return ((Number) count).longValue();
    }
}
