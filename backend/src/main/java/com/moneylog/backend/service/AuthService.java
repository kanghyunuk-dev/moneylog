package com.moneylog.backend.service;

import com.moneylog.backend.dto.request.RegisterRequest;
import com.moneylog.backend.dto.response.UserResponse;
import com.moneylog.backend.entity.User;
import com.moneylog.backend.exception.DuplicateEmailException;
import com.moneylog.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

}
