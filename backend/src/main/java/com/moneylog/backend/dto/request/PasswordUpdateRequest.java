package com.moneylog.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PasswordUpdateRequest(
  @NotBlank
  String currentPassword,

  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$", message = "비밀번호는 영문+숫자 조합 8자 이상이어야 합니다")
  String newPassword
) {}
