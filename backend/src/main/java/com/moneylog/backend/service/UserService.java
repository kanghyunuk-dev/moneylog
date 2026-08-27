package com.moneylog.backend.service;

import com.moneylog.backend.dto.request.NicknameUpdateRequest;
import com.moneylog.backend.dto.request.PasswordUpdateRequest;
import com.moneylog.backend.dto.response.UserResponse;
import com.moneylog.backend.entity.User;
import com.moneylog.backend.exception.InvalidCredentialsException;
import com.moneylog.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getMyInfo(User user) {
        return UserResponse.from(user);
    }

    @Transactional
    public void updateNickname(User user, NicknameUpdateRequest request) {
        User managedUser = userRepository.findById(user.getId())
                        .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자 입니다"));
        managedUser.changeNickname(request.nickname());
    }

    @Transactional
    public void updatePassword(User user, PasswordUpdateRequest request) {
        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 사용자 입니다"));

        // 1. 현재 비밀번호 검증
        if(!passwordEncoder.matches(request.currentPassword(), managedUser.getPassword())) {
            throw new InvalidCredentialsException("현재 비밀번호가 일치하지 않습니다");
        }

        // 2. 새 비밀번호 암호화 후 변경
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        managedUser.changePassword(encodedPassword);
    }
}
