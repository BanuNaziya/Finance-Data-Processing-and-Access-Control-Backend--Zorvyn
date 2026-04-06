package com.finance.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyTrendDto {

    /** Year-month string in format YYYY-MM (e.g., "2026-04") */
    private String month;

    /** Total income for this month */
    private BigDecimal totalIncome;

    /** Total expenses for this month */
    private BigDecimal totalExpenses;

    /** Net balance = totalIncome - totalExpenses */
    private BigDecimal netBalance;

    /** Total number of transactions this month */
    private long transactionCount;
}
