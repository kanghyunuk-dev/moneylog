package com.moneylog.backend.controller;

import com.moneylog.backend.dto.request.GoalCreateRequest;
import com.moneylog.backend.dto.request.GoalUpdateRequest;
import com.moneylog.backend.dto.response.GoalResponse;
import com.moneylog.backend.security.PrincipalDetails;
import com.moneylog.backend.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {
    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@AuthenticationPrincipal PrincipalDetails principalDetails, @Valid @RequestBody GoalCreateRequest request) {
        GoalResponse response = goalService.createGoal(principalDetails.getUser(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<GoalResponse>> getGoals(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        return ResponseEntity.ok(goalService.getGoals(principalDetails.getUser()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> updateGoal(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long id, @Valid @RequestBody GoalUpdateRequest request) {
        return ResponseEntity.ok(goalService.updateGoal(principalDetails.getUser(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoal(@AuthenticationPrincipal PrincipalDetails principalDetails, @PathVariable Long id) {
        goalService.deleteGoal(principalDetails.getUser(), id);
        return ResponseEntity.ok().build();
    }
}
