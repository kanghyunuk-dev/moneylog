package com.moneylog.backend.security;


import io.jsonwebtoken.MalformedJwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private JWTTokenProvider jwtTokenProvider;

    @InjectMocks
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void Redis에_장애가_나면_블랙리스트_확인은_통과_처리된다() {
        // given
        given(redisTemplate.hasKey(anyString())).willThrow(new QueryTimeoutException("Redis command timed out"));

        // when
        boolean result = tokenBlacklistService.isBlacklisted("some-token");

        // then
        assertThat(result).isFalse();
    }

    @Test
    void 유효하지_않은_토큰이면_블랙리스트_등록을_건너뛴다() {
        // given
        given(jwtTokenProvider.getExpirationFromToken(anyString())).willThrow(new MalformedJwtException("Invalid token"));

        // when & then
        assertThatCode(() -> tokenBlacklistService.blacklist("broken-token"))
                .doesNotThrowAnyException();

        then(redisTemplate).shouldHaveNoInteractions();
    }

}
