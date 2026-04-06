package com.finance.backend.repository;

import com.finance.backend.dto.CategoryTotalDto;
import com.finance.backend.model.Transaction;
import com.finance.backend.model.TransactionType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {

    
    Optional<Transaction> findByIdAndDeletedFalse(Long id);

    
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.deleted = false AND t.type = :type")
    BigDecimal sumByType(@Param("type") TransactionType type);

    /**
     * Count transactions by type (for the summary count fields).
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.deleted = false AND t.type = :type")
    long countByType(@Param("type") TransactionType type);

    /**
     * Total transaction count (all non-deleted).
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.deleted = false")
    long countActive();

    
    @Query("SELECT new com.finance.backend.dto.CategoryTotalDto(" +
           "  t.category, t.type, SUM(t.amount), COUNT(t)) " +
           "FROM Transaction t " +
           "WHERE t.deleted = false " +
           "GROUP BY t.category, t.type " +
           "ORDER BY SUM(t.amount) DESC")
    List<CategoryTotalDto> getCategoryTotals();

    
    @Query("SELECT YEAR(t.date), MONTH(t.date), t.type, SUM(t.amount), COUNT(t) " +
           "FROM Transaction t " +
           "WHERE t.deleted = false AND t.date >= :cutoffDate " +
           "GROUP BY YEAR(t.date), MONTH(t.date), t.type " +
           "ORDER BY YEAR(t.date) ASC, MONTH(t.date) ASC")
    List<Object[]> getMonthlyRawData(@Param("cutoffDate") LocalDate cutoffDate);

    
    @Query("SELECT t FROM Transaction t " +
           "JOIN FETCH t.createdBy u " +
           "WHERE t.deleted = false " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findRecentTransactions(Pageable pageable);
}
