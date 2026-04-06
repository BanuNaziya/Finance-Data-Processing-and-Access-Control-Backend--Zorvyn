package com.finance.backend.service;

import com.finance.backend.dto.CategoryTotalDto;
import com.finance.backend.dto.DashboardSummaryDto;
import com.finance.backend.dto.MonthlyTrendDto;
import com.finance.backend.dto.TransactionDto;
import com.finance.backend.model.Transaction;
import com.finance.backend.model.TransactionType;
import com.finance.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;

    
    public DashboardSummaryDto getSummary() {

        // Query the DB for aggregated values
        // SUM queries return BigDecimal; COUNT queries return long
        BigDecimal totalIncome = transactionRepository.sumByType(TransactionType.INCOME);
        BigDecimal totalExpenses = transactionRepository.sumByType(TransactionType.EXPENSE);

        long incomeCount = transactionRepository.countByType(TransactionType.INCOME);
        long expenseCount = transactionRepository.countByType(TransactionType.EXPENSE);
        long totalCount = transactionRepository.countActive();

        // Net balance = income - expenses
        // BigDecimal.subtract() is used for exact decimal arithmetic
        BigDecimal netBalance = totalIncome.subtract(totalExpenses);

        return DashboardSummaryDto.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netBalance(netBalance)
                .transactionCount(totalCount)
                .incomeCount(incomeCount)
                .expenseCount(expenseCount)
                .build();
    }

    
    public List<CategoryTotalDto> getCategoryTotals() {
        // The query in TransactionRepository does the heavy lifting
        return transactionRepository.getCategoryTotals();
    }

   
    public List<MonthlyTrendDto> getMonthlyTrends(int monthsBack) {
        // Calculate the cutoff date (go back N months from today)
        LocalDate cutoffDate = LocalDate.now().minusMonths(monthsBack);

        // Get raw data from repository
        // Each Object[] = [year(int), month(int), type(TransactionType), sum(BigDecimal), count(long)]
        List<Object[]> rawData = transactionRepository.getMonthlyRawData(cutoffDate);

        // Use a LinkedHashMap to preserve insertion order when we sort later
        // Key: "YYYY-MM" string, Value: MonthlyTrendDto builder accumulating data
        Map<String, MonthlyTrendDto> monthMap = new LinkedHashMap<>();

        for (Object[] row : rawData) {
            // Extract and cast each column from the Object[] array
            int year  = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            TransactionType type  = (TransactionType) row[2];
            BigDecimal amount     = (BigDecimal) row[3];
            long count            = ((Number) row[4]).longValue();

            // Format as "YYYY-MM" (e.g., "2026-04")
            String monthKey = String.format("%04d-%02d", year, month);

            // Get or create entry for this month
            MonthlyTrendDto dto = monthMap.computeIfAbsent(monthKey, k ->
                    MonthlyTrendDto.builder()
                            .month(k)
                            .totalIncome(BigDecimal.ZERO)
                            .totalExpenses(BigDecimal.ZERO)
                            .netBalance(BigDecimal.ZERO)
                            .transactionCount(0)
                            .build()
            );

            // Add the amount to the appropriate field based on type
            if (type == TransactionType.INCOME) {
                dto.setTotalIncome(dto.getTotalIncome().add(amount));
            } else {
                dto.setTotalExpenses(dto.getTotalExpenses().add(amount));
            }

            // Accumulate transaction count
            dto.setTransactionCount(dto.getTransactionCount() + count);
        }

        // Final pass: calculate net balance for each month
        // net = income - expenses
        monthMap.values().forEach(dto ->
                dto.setNetBalance(dto.getTotalIncome().subtract(dto.getTotalExpenses()))
        );

        // Return sorted by month (the map is already insertion-ordered,
        // and the DB query returns data in chronological order)
        return new ArrayList<>(monthMap.values());
    }

   
    public List<TransactionDto> getRecentTransactions(int limit) {
        // Cap at 50 to prevent large result sets
        limit = Math.min(50, Math.max(1, limit));

        // PageRequest.of(0, limit) = first page, N items
        List<Transaction> recent = transactionRepository.findRecentTransactions(
                PageRequest.of(0, limit)
        );

        // Map entities to DTOs
        return recent.stream()
                .map(TransactionDto::fromTransaction)
                .collect(Collectors.toList());
    }
}
