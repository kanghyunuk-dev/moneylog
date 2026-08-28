package com.moneylog.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record RegisterRequest(
        @Email
        @NotBlank
        String email,

        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = "비밀번호는 영문+숫자 조합 8자 이상이어야 합니다")
        String password,

        @NotBlank
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요")
        String nickname
) {}
