package com.moneylog.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameUpdateRequest(
        @NotBlank
        @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하로 입력해주세요")
        String nickname
) {}
