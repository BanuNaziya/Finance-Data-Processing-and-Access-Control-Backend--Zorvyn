package com.finance.backend.dto;

import com.finance.backend.model.Transaction;
import com.finance.backend.model.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private LocalDate date;
    private String notes;
    private Long createdById;
    private String createdByUsername;  // Flat field — no nested User object
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    
    public static TransactionDto fromTransaction(Transaction t) {
        return TransactionDto.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .type(t.getType())
                .category(t.getCategory())
                .date(t.getDate())
                .notes(t.getNotes())
                .createdById(t.getCreatedBy().getId())
                .createdByUsername(t.getCreatedBy().getUsername())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
