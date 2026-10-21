package com.moneylog.backend.service;

import com.moneylog.backend.dto.request.LoginRequest;
import com.moneylog.backend.dto.request.RegisterRequest;
import com.moneylog.backend.dto.response.TokenResponse;
import com.moneylog.backend.entity.User;
import com.moneylog.backend.repository.RefreshTokenRepository;
import com.moneylog.backend.repository.UserRepository;
import com.moneylog.backend.security.JWTTokenProvider;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// 로그인·체험 로그인 토큰 발급 통합 테스트 (테스트 후 롤백)
@SpringBootTest
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class AuthServiceTest {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTTokenProvider jwtTokenProvider;

    @Test
    void 일반_로그인은_토큰을_발급하고_RefreshToken을_저장한다() {
        // given
        String email = "login-" + UUID.randomUUID() + "@test.com";
        authService.register(new RegisterRequest(email, "password123", "테스트"));

        // when
        TokenResponse tokens = authService.login(new LoginRequest(email, "password123"));

        // then
        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(jwtTokenProvider.getEmailFromToken(tokens.accessToken())).isEqualTo(email);
        assertThat(refreshTokenRepository.findByUser(user))
                .hasValueSatisfying(saved -> assertThat(saved.getToken()).isEqualTo(tokens.refreshToken()));
    }

    @Test
    void 체험_로그인은_DEMO_계정을_만들고_토큰을_발급한다() {
        // when
        TokenResponse tokens = authService.loginAsDemo();

        // then
        String email = jwtTokenProvider.getEmailFromToken(tokens.accessToken());
        User user = userRepository.findByEmail(email).orElseThrow();
        assertThat(user.getProvider()).isEqualTo(DemoService.PROVIDER);
        assertThat(refreshTokenRepository.findByUser(user))
                .hasValueSatisfying(saved -> assertThat(saved.getToken()).isEqualTo(tokens.refreshToken()));
    }
}
