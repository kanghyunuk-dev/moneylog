package com.moneylog.backend.scheduler;

import com.moneylog.backend.entity.User;
import com.moneylog.backend.repository.UserRepository;
import com.moneylog.backend.service.DemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {

    private final UserRepository userRepository;

    // 매일 새벽 4시 30분 실행 (초 분 시 일 월 요일)
    @Scheduled(cron = "0 30 4 * * *")
    @Transactional
    public void cleanupWithdrawnUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<User> targets = userRepository.findByDeletedAtBefore(threshold);

        if(targets.isEmpty()) {
            return;
        }

        userRepository.deleteAll(targets);
        log.info("탈퇴 30일 경과 회원 {}명 영구삭제 완료", targets.size());
    }

    // 매시 정각 실행 - 생성 24시간 지난 체험 계정 삭제 (하위 데이터는 DB CASCADE)
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupDemoUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<User> targets = userRepository.findByProviderAndCreatedAtBefore(DemoService.PROVIDER, threshold);

        if(targets.isEmpty()) {
            return;
        }

        userRepository.deleteAll(targets);
        log.info("생성 24시간 경과 체험 계정 {}개 삭제 완료", targets.size());
    }

}
