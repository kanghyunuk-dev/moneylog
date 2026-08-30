package com.moneylog.backend.controller;

import com.moneylog.backend.dto.request.NicknameUpdateRequest;
import com.moneylog.backend.dto.request.PasswordUpdateRequest;
import com.moneylog.backend.dto.request.WithdrawRequest;
import com.moneylog.backend.dto.response.UserResponse;
import com.moneylog.backend.security.CookieUtils;
import com.moneylog.backend.security.PrincipalDetails;
import com.moneylog.backend.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        UserResponse response = userService.getMyInfo(principalDetails.getUser());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateNickname(@AuthenticationPrincipal PrincipalDetails principalDetails, @Valid @RequestBody NicknameUpdateRequest request) {
        userService.updateNickname(principalDetails.getUser(), request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal PrincipalDetails principalDetails, @Valid @RequestBody PasswordUpdateRequest request) {
        userService.updatePassword(principalDetails.getUser(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@AuthenticationPrincipal PrincipalDetails principalDetails, @Valid @RequestBody WithdrawRequest request, HttpServletResponse response) {

        userService.withdraw(principalDetails.getUser(), request);

        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.build("accessToken", "", Duration.ZERO).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.build("refreshToken", "", Duration.ZERO).toString());

        return ResponseEntity.ok().build();
    }

}
