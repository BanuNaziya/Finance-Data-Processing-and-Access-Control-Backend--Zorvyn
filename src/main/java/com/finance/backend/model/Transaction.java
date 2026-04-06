package com.finance.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    /** Auto-incremented primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    
    @Column(nullable = false, length = 100)
    private String category;

    
    @Column(nullable = false)
    private LocalDate date;

   
    @Column(columnDefinition = "TEXT")
    private String notes;

   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User createdBy;

    
    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    /** Set once when the record is first inserted */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** Automatically updated whenever the record changes */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
