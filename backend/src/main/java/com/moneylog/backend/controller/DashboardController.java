package com.moneylog.backend.controller;

import com.moneylog.backend.dto.response.CategoryBreakdownItem;
import com.moneylog.backend.dto.response.DashboardSummaryResponse;
import com.moneylog.backend.dto.response.MonthlyTrendItem;
import com.moneylog.backend.security.PrincipalDetails;
import com.moneylog.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ResponseEntity.ok(dashboardService.getSummary(principalDetails.getUser(), month));
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryBreakdownItem>> getCategoryBreakdown(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ResponseEntity.ok(dashboardService.getCategoryBreakdown(principalDetails.getUser(), month));
    }

    @GetMapping("/monthly-trend")
    public ResponseEntity<List<MonthlyTrendItem>> getMonthlyTrend(@AuthenticationPrincipal PrincipalDetails principalDetails, @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month, @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(dashboardService.getMonthlyTrend(principalDetails.getUser(), month, months));
    }

}
