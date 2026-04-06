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
public class DashboardSummaryDto {

    /** Sum of all income transactions */
    private BigDecimal totalIncome;

    /** Sum of all expense transactions */
    private BigDecimal totalExpenses;

    /** totalIncome - totalExpenses (can be negative) */
    private BigDecimal netBalance;

    /** Total number of transactions (income + expense) */
    private long transactionCount;

    /** Number of income transactions */
    private long incomeCount;

    /** Number of expense transactions */
    private long expenseCount;
}
