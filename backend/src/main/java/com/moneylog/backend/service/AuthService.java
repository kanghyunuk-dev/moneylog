package com.moneylog.backend.service;

import com.moneylog.backend.dto.request.LoginRequest;
import com.moneylog.backend.dto.request.RefreshTokenRequest;
import com.moneylog.backend.dto.request.RegisterRequest;
import com.moneylog.backend.dto.response.TokenResponse;
import com.moneylog.backend.dto.response.UserResponse;
import com.moneylog.backend.entity.RefreshToken;
import com.moneylog.backend.entity.User;
import com.moneylog.backend.exception.DuplicateEmailException;
import com.moneylog.backend.exception.InvalidCredentialsException;
import com.moneylog.backend.exception.InvalidTokenException;
import com.moneylog.backend.repository.RefreshTokenRepository;
import com.moneylog.backend.repository.UserRepository;
import com.moneylog.backend.security.JWTTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("이미 가입 된 이메일 입니다");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.builder()
                .email(request.email())
                .password(encodedPassword)
                .nickname(request.nickname())
                .provider("LOCAL")
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);

    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        // 1. 이메일+비밀번호 인증 (실패 시 BadCredentialsException 자동 발생)
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            throw new InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일 입니다"));

        // 2. AccessToken/RefreshToken 발급
        String accessToken = jwtTokenProvider.createAccessToken(user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

        // 3. 기존 RefreshToken 있으면 교체, 없으면 신규 저장
        updateRefreshToken(user, refreshToken);

        // 4. 토큰 응답 반환
        return new TokenResponse(accessToken, refreshToken);
    }

    private void updateRefreshToken(User user, String newRefreshToken) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        refreshTokenRepository.findByUser(user)
                .ifPresentOrElse(
                        refreshToken -> refreshToken.updateToken(newRefreshToken, expiresAt),
                        () -> refreshTokenRepository.save(
                                RefreshToken.builder()
                                        .user(user)
                                        .token(newRefreshToken)
                                        .expiresAt(expiresAt)
                                        .build()
                        )
                );
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidTokenException("유효하지 않은 토큰 입니다");
        }

        RefreshToken savedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("존재하지 않는 토큰 입니다"));

        String email = savedToken.getUser().getEmail();
        String newAccessToken = jwtTokenProvider.createAccessToken(email);

        return new TokenResponse(newAccessToken, refreshToken);
    }

}
