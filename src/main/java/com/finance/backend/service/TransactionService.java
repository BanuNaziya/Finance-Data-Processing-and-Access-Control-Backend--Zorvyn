package com.finance.backend.service;

import com.finance.backend.dto.TransactionDto;
import com.finance.backend.dto.TransactionRequest;
import com.finance.backend.exception.AppException;
import com.finance.backend.model.Transaction;
import com.finance.backend.model.TransactionType;
import com.finance.backend.model.User;
import com.finance.backend.repository.TransactionRepository;
import com.finance.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public TransactionDto createTransaction(TransactionRequest request, Long creatorId) {
        // Validate required fields for creation
        if (request.getAmount() == null) {
            throw new AppException("Amount is required.", HttpStatus.BAD_REQUEST);
        }
        if (request.getType() == null) {
            throw new AppException("Type (INCOME or EXPENSE) is required.", HttpStatus.BAD_REQUEST);
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            throw new AppException("Category is required.", HttpStatus.BAD_REQUEST);
        }
        if (request.getDate() == null) {
            throw new AppException("Date is required.", HttpStatus.BAD_REQUEST);
        }

        // Load the user who is creating this transaction
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new AppException("Creator user not found.", HttpStatus.NOT_FOUND));

        // Build the Transaction entity using the builder pattern
        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory().trim())  // Trim whitespace
                .date(request.getDate())
                .notes(request.getNotes())
                .createdBy(creator)
                .deleted(false)  // New records are never deleted
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return TransactionDto.fromTransaction(saved);
    }

    
    public Page<TransactionDto> getTransactions(
            String type, String category,
            LocalDate startDate, LocalDate endDate,
            BigDecimal minAmount, BigDecimal maxAmount,
            int page, int size,
            String sortBy, String sortDir) {

        // Validate and cap page size for performance
        size = Math.min(100, Math.max(1, size));

        // Build sort object
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // ── Build Specification (dynamic WHERE clause) ────────────────────────

        // Always exclude soft-deleted records
        Specification<Transaction> spec = (root, query, cb) ->
                cb.equal(root.get("deleted"), false);

        // Filter by transaction type
        if (type != null && !type.isBlank()) {
            try {
                TransactionType transactionType = TransactionType.valueOf(type.toUpperCase());
                spec = spec.and((root, query, cb) ->
                        cb.equal(root.get("type"), transactionType)
                );
            } catch (IllegalArgumentException e) {
                throw new AppException(
                        "Invalid type. Must be INCOME or EXPENSE.", HttpStatus.BAD_REQUEST
                );
            }
        }

        // Filter by category (case-insensitive partial match)
        if (category != null && !category.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("category")), "%" + category.toLowerCase() + "%")
            );
        }

        // Date range filters
        if (startDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("date"), startDate)
            );
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("date"), endDate)
            );
        }

        // Amount range filters
        if (minAmount != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("amount"), minAmount)
            );
        }
        if (maxAmount != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("amount"), maxAmount)
            );
        }

        // Execute the query and map results to DTOs
        return transactionRepository.findAll(spec, pageable)
                .map(TransactionDto::fromTransaction);
    }

    
    public TransactionDto getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(
                        "Transaction with ID " + id + " not found.", HttpStatus.NOT_FOUND
                ));
        return TransactionDto.fromTransaction(transaction);
    }

   
    public TransactionDto updateTransaction(Long id, TransactionRequest request) {
        // Find the transaction (excludes deleted records)
        Transaction transaction = transactionRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new AppException(
                        "Transaction with ID " + id + " not found.", HttpStatus.NOT_FOUND
                ));

        // Apply only the fields that were provided in the request
        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getType() != null) {
            transaction.setType(request.getType());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            transaction.setCategory(request.getCategory().trim());
        }
        if (request.getDate() != null) {
            transaction.setDate(request.getDate());
        }
        if (request.getNotes() != null) {
            // Allow setting notes to empty string (clearing the note)
            transaction.setNotes(request.getNotes());
        }

        Transaction updated = transactionRepository.save(transaction);
        return TransactionDto.fromTransaction(updated);
    }

    
    public void deleteTransaction(Long id) {
        // Look up the transaction regardless of deleted status
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new AppException(
                        "Transaction with ID " + id + " not found.", HttpStatus.NOT_FOUND
                ));

        // Prevent double-deletion (idempotency check)
        if (transaction.isDeleted()) {
            throw new AppException("Transaction is already deleted.", HttpStatus.BAD_REQUEST);
        }

        // Mark as deleted (soft delete)
        transaction.setDeleted(true);
        transactionRepository.save(transaction);
    }
}
