package com.moneylog.backend.controller;

import com.moneylog.backend.dto.request.NicknameUpdateRequest;
import com.moneylog.backend.dto.request.PasswordUpdateRequest;
import com.moneylog.backend.dto.response.UserResponse;
import com.moneylog.backend.security.PrincipalDetails;
import com.moneylog.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

}
