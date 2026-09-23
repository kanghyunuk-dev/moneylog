package com.moneylog.backend.security;

import com.moneylog.backend.entity.User;
import com.moneylog.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class PrincipalDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Object cachedObj = redisTemplate.opsForValue().get(UserAuthCache.KEY_PREFIX + email);
        UserAuthCache cached = cachedObj instanceof UserAuthCache ? (UserAuthCache) cachedObj : null;

        if (cached != null && cached.deleted()) {
            throw new UsernameNotFoundException("탈퇴한 계정 입니다");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일 입니다"));

        if (cached == null) {
            redisTemplate.opsForValue().set(UserAuthCache.KEY_PREFIX + email, new UserAuthCache(user.getId(), user.getDeletedAt() != null), CACHE_TTL);
        }

        if(user.getDeletedAt() != null) {
            throw new UsernameNotFoundException("탈퇴한 계정 입니다");
        }

        return new PrincipalDetails(user);
    }

}
