package com.finance.backend.controller;

import com.finance.backend.dto.ApiResponse;
import com.finance.backend.dto.TransactionDto;
import com.finance.backend.dto.TransactionRequest;
import com.finance.backend.security.JwtUtil;
import com.finance.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;


@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtUtil jwtUtil;

   
    @PostMapping
    public ResponseEntity<ApiResponse<TransactionDto>> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader("Authorization") String authHeader) {

        // Extract the user ID from the JWT token to set as creator
        String token = authHeader.substring(7); // Remove "Bearer "
        Long creatorId = jwtUtil.extractUserId(token);

        TransactionDto created = transactionService.createTransaction(request, creatorId);

        // 201 Created is the correct HTTP status for resource creation
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Transaction created successfully.", created));
    }

    
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TransactionDto>>> getTransactions(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Page<TransactionDto> transactions = transactionService.getTransactions(
                type, category, startDate, endDate,
                minAmount, maxAmount, page, size, sortBy, sortDir
        );

        return ResponseEntity.ok(
                ApiResponse.success("Transactions retrieved successfully.", transactions)
        );
    }

    /**
     * getTransactionById - GET /api/transactions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDto>> getTransactionById(
            @PathVariable Long id) {

        TransactionDto transaction = transactionService.getTransactionById(id);

        return ResponseEntity.ok(
                ApiResponse.success("Transaction retrieved successfully.", transaction)
        );
    }

    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionDto>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {

        TransactionDto updated = transactionService.updateTransaction(id, request);

        return ResponseEntity.ok(
                ApiResponse.success("Transaction updated successfully.", updated)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);

        return ResponseEntity.ok(
                ApiResponse.success("Transaction deleted successfully.")
        );
    }
}
