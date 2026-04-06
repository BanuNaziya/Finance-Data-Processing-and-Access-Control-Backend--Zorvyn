package com.finance.backend.dto;

import com.finance.backend.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTotalDto {

    /** Category name (e.g., "Salary", "Rent") */
    private String category;

    /** Transaction type (INCOME or EXPENSE) */
    private TransactionType type;

    /** Total amount for this category + type combination */
    private BigDecimal totalAmount;

    /** Number of transactions in this category + type */
    private long transactionCount;
}
