package com.finance.backend.controller;

import com.finance.backend.dto.*;
import com.finance.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

   
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getSummary() {
        DashboardSummaryDto summary = dashboardService.getSummary();
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard summary retrieved successfully.", summary)
        );
    }

    
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryTotalDto>>> getCategoryTotals() {
        List<CategoryTotalDto> categories = dashboardService.getCategoryTotals();
        return ResponseEntity.ok(
                ApiResponse.success("Category totals retrieved successfully.", categories)
        );
    }

    @GetMapping("/monthly-trends")
    public ResponseEntity<ApiResponse<List<MonthlyTrendDto>>> getMonthlyTrends(
            @RequestParam(defaultValue = "12") int months) {

        // Cap at 24 months to prevent excessive data fetching
        months = Math.min(24, Math.max(1, months));

        List<MonthlyTrendDto> trends = dashboardService.getMonthlyTrends(months);
        return ResponseEntity.ok(
                ApiResponse.success("Monthly trends retrieved successfully.", trends)
        );
    }

    
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<TransactionDto>>> getRecentTransactions(
            @RequestParam(defaultValue = "10") int limit) {

        List<TransactionDto> recent = dashboardService.getRecentTransactions(limit);
        return ResponseEntity.ok(
                ApiResponse.success("Recent transactions retrieved successfully.", recent)
        );
    }

    
    @GetMapping("/health-check")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        return ResponseEntity.ok(
                ApiResponse.success("Dashboard service is operational.", "OK")
        );
    }
}
