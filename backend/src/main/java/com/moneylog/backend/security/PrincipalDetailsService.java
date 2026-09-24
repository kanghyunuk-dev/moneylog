package com.moneylog.backend.security;

import com.moneylog.backend.entity.User;
import com.moneylog.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAuthCache cached = null;
        boolean redisFailed = false;

        try {
            Object cachedObj = redisTemplate.opsForValue().get(UserAuthCache.KEY_PREFIX + email);
            cached = cachedObj instanceof UserAuthCache ? (UserAuthCache) cachedObj : null;
        } catch (DataAccessException e) {
            redisFailed = true;
            log.warn("Redis 장애로 캐시 조회 실패, DB로 진행: {}", e.toString());
        }

        if (cached != null && cached.deleted()) {
            throw new UsernameNotFoundException("탈퇴한 계정 입니다");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일 입니다"));

        if (cached == null && !redisFailed) {
            try {
                redisTemplate.opsForValue().set(UserAuthCache.KEY_PREFIX + email, new UserAuthCache(user.getId(), user.getDeletedAt() != null), CACHE_TTL);
            } catch (DataAccessException e) {
                log.warn("Redis 장애로 캐시 저장 실패: {}", e.toString());
            }
        }

        if(user.getDeletedAt() != null) {
            throw new UsernameNotFoundException("탈퇴한 계정 입니다");
        }

        return new PrincipalDetails(user);
    }

}
