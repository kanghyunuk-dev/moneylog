package com.moneylog.backend.service;

import com.moneylog.backend.entity.Budget;
import com.moneylog.backend.entity.Category;
import com.moneylog.backend.entity.CategoryType;
import com.moneylog.backend.entity.Goal;
import com.moneylog.backend.entity.GoalStatus;
import com.moneylog.backend.entity.Transaction;
import com.moneylog.backend.entity.User;
import com.moneylog.backend.exception.CategoryNotFoundException;
import com.moneylog.backend.exception.DemoLimitExceededException;
import com.moneylog.backend.repository.BudgetRepository;
import com.moneylog.backend.repository.CategoryRepository;
import com.moneylog.backend.repository.GoalRepository;
import com.moneylog.backend.repository.TransactionRepository;
import com.moneylog.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemoService {

    public static final String PROVIDER = "DEMO";

    // 시간당 전체 생성 한도 (IP 분산 우회 대비, 동시 요청 시 약간 초과 가능)
    public static final int HOURLY_LIMIT = 100;

    // 생성 기간 (통계 추이 차트 기본값 6개월)
    private static final int MONTHS = 6;

    private static final List<String> FOOD_MEMOS = List.of("점심", "저녁", "장보기", "카페", "배달 음식");
    private static final List<String> TRANSPORT_MEMOS = List.of("지하철", "버스", "택시");
    private static final List<String> SHOPPING_MEMOS = List.of("의류 구매", "온라인 쇼핑", "신발 구매");

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final GoalRepository goalRepository;

    // 체험 계정 + 샘플 데이터(6개월 거래, 이번 달 예산, 목표) 생성
    @Transactional
    public User createDemoUser() {
        long recentCount = userRepository.countByProviderAndCreatedAtAfter(PROVIDER, LocalDateTime.now().minusHours(1));
        if (recentCount >= HOURLY_LIMIT) {
            throw new DemoLimitExceededException("체험 계정 요청이 많습니다. 잠시 후 다시 시도해주세요");
        }

        User user = userRepository.save(User.builder()
                .email("demo-" + UUID.randomUUID() + "@moneylog.demo")
                .nickname("체험 사용자")
                .provider(PROVIDER)
                .build());

        // 타입+이름으로 조회 ("기타"가 수입·지출 양쪽에 있음)
        Map<String, Category> categories = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(c -> key(c.getType(), c.getName()), Function.identity()));

        LocalDate today = LocalDate.now();
        YearMonth thisMonth = YearMonth.from(today);

        transactionRepository.saveAll(createTransactions(user, categories, today));
        budgetRepository.saveAll(createBudgets(user, categories, thisMonth));
        goalRepository.saveAll(createGoals(user, thisMonth));

        return user;
    }

    private List<Transaction> createTransactions(User user, Map<String, Category> categories, LocalDate today) {
        Category salary = category(categories, CategoryType.INCOME, "급여");
        Category sideIncome = category(categories, CategoryType.INCOME, "부수입");
        Category interest = category(categories, CategoryType.INCOME, "금융소득");
        Category food = category(categories, CategoryType.EXPENSE, "식비");
        Category transport = category(categories, CategoryType.EXPENSE, "교통비");
        Category housing = category(categories, CategoryType.EXPENSE, "주거비");
        Category phone = category(categories, CategoryType.EXPENSE, "통신비");
        Category culture = category(categories, CategoryType.EXPENSE, "문화/여가");
        Category health = category(categories, CategoryType.EXPENSE, "의료/건강");
        Category shopping = category(categories, CategoryType.EXPENSE, "쇼핑");
        Category education = category(categories, CategoryType.EXPENSE, "교육");
        Category gift = category(categories, CategoryType.EXPENSE, "경조사/선물");
        Category etc = category(categories, CategoryType.EXPENSE, "기타");

        List<Transaction> transactions = new ArrayList<>();
        YearMonth thisMonth = YearMonth.from(today);

        for (int i = MONTHS - 1; i >= 0; i--) {
            YearMonth month = thisMonth.minusMonths(i);

            // 고정 수입·지출 (급여는 1일 - 월초에도 이번 달 수입 표시)
            transactions.add(transaction(user, salary, 2_800_000, month.atDay(1), "월급"));
            transactions.add(transaction(user, housing, 550_000, month.atDay(5), "월세"));
            transactions.add(transaction(user, housing, amount(90_000, 130_000), month.atDay(25), "관리비"));
            transactions.add(transaction(user, phone, 55_000, month.atDay(15), "휴대폰 요금"));
            transactions.add(transaction(user, culture, 13_500, month.atDay(1), "넷플릭스 구독"));
            transactions.add(transaction(user, etc, 200_000, month.atDay(10), "부모님 용돈"));
            transactions.add(transaction(user, interest, amount(8_000, 15_000), month.atDay(28), "예금 이자"));

            // 쇼핑·문화 (1일 쇼핑으로 이번 달 쇼핑 예산 항상 초과)
            transactions.add(transaction(user, shopping, amount(110_000, 160_000), month.atDay(1), pick(SHOPPING_MEMOS)));
            transactions.add(transaction(user, shopping, amount(20_000, 60_000), month.atDay(16), "생활용품"));
            transactions.add(transaction(user, culture, amount(15_000, 40_000), month.atDay(13), "영화"));

            // 변동 지출 (식비 3일, 교통비 4일 간격)
            for (int day = 1; day <= month.lengthOfMonth(); day += 3) {
                transactions.add(transaction(user, food, amount(7_000, 25_000), month.atDay(day), pick(FOOD_MEMOS)));
            }
            for (int day = 1; day <= month.lengthOfMonth(); day += 4) {
                transactions.add(transaction(user, transport, amount(1_400, 9_000), month.atDay(day), pick(TRANSPORT_MEMOS)));
            }

            // 비정기 항목 (달마다 다르게)
            if (i % 2 == 0) {
                transactions.add(transaction(user, health, amount(10_000, 40_000), month.atDay(20), "병원"));
            } else {
                transactions.add(transaction(user, education, amount(30_000, 70_000), month.atDay(9), "온라인 강의"));
            }
            if (i % 3 == 0) {
                transactions.add(transaction(user, gift, amount(50_000, 100_000), month.atDay(22), "친구 생일 선물"));
            } else if (i % 3 == 1) {
                transactions.add(transaction(user, sideIncome, amount(30_000, 120_000), month.atDay(18), "중고 거래"));
            }
        }

        // 미래 날짜 거래 제외
        transactions.removeIf(t -> t.getTransactionDate().isAfter(today));
        return transactions;
    }

    // 이번 달 예산 (쇼핑만 초과)
    private List<Budget> createBudgets(User user, Map<String, Category> categories, YearMonth thisMonth) {
        String budgetMonth = thisMonth.toString();

        return List.of(
                budget(user, category(categories, CategoryType.EXPENSE, "식비"), budgetMonth, 300_000),
                budget(user, category(categories, CategoryType.EXPENSE, "교통비"), budgetMonth, 100_000),
                budget(user, category(categories, CategoryType.EXPENSE, "쇼핑"), budgetMonth, 100_000),
                budget(user, category(categories, CategoryType.EXPENSE, "문화/여가"), budgetMonth, 100_000)
        );
    }

    // 목표 (조회 시 순저축이 목표를 넘으면 자동 완료되므로 넉넉히 설정)
    private List<Goal> createGoals(User user, YearMonth thisMonth) {
        LocalDateTime activeStart = thisMonth.minusMonths(1).atDay(1).atTime(9, 0);
        LocalDateTime doneStart = thisMonth.minusMonths(MONTHS - 1).atDay(1).atTime(9, 0);

        return List.of(
                goal(user, "첫 월급 100만원 모으기", 1_000_000, 1_000_000, GoalStatus.DONE, 0, doneStart),
                goal(user, "비상금 1,000만원 모으기", 10_000_000, 0, GoalStatus.ACTIVE, 1, activeStart),
                goal(user, "여행 자금 200만원", 2_000_000, 0, GoalStatus.WAITING, 2, null),
                goal(user, "노트북 구입 150만원", 1_500_000, 0, GoalStatus.WAITING, 3, null)
        );
    }

    private Category category(Map<String, Category> categories, CategoryType type, String name) {
        Category category = categories.get(key(type, name));
        if (category == null) {
            throw new CategoryNotFoundException("존재하지 않는 카테고리 입니다: " + key(type, name));
        }
        return category;
    }

    private String key(CategoryType type, String name) {
        return type + ":" + name;
    }

    private Transaction transaction(User user, Category category, long amount, LocalDate date, String memo) {
        return Transaction.builder()
                .user(user)
                .category(category)
                .amount(amount)
                .transactionDate(date)
                .memo(memo)
                .build();
    }

    private Budget budget(User user, Category category, String budgetMonth, long amount) {
        return Budget.builder()
                .user(user)
                .category(category)
                .budgetMonth(budgetMonth)
                .amount(amount)
                .build();
    }

    private Goal goal(User user, String name, long targetAmount, long currentAmount, GoalStatus status, int displayOrder, LocalDateTime activatedAt) {
        return Goal.builder()
                .user(user)
                .name(name)
                .targetAmount(targetAmount)
                .currentAmount(currentAmount)
                .status(status)
                .displayOrder(displayOrder)
                .activatedAt(activatedAt)
                .build();
    }

    // min~max 금액 (100원 단위)
    private long amount(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1) / 100 * 100L;
    }

    private String pick(List<String> memos) {
        return memos.get(ThreadLocalRandom.current().nextInt(memos.size()));
    }
}
