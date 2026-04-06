package com.finance.backend.dto;

import com.finance.backend.model.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


@Data
public class TransactionRequest {

   
    @Positive(message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must have at most 10 integer digits and 2 decimal places")
    private BigDecimal amount;

    
    private TransactionType type;

   
    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate date;

  
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
