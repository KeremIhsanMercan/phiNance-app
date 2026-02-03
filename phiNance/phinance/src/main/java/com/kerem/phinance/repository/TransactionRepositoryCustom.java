package com.kerem.phinance.repository;

import com.kerem.phinance.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface TransactionRepositoryCustom {
    Page<Transaction> findByFilters(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            String accountId,
            String categoryId,
            String type,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String searchQuery,
            Pageable pageable
    );

    List<Transaction> findAllByFilters(
            String userId,
            LocalDate startDate,
            LocalDate endDate,
            String accountId,
            String categoryId,
            String type,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String searchQuery,
            Sort sort
    );
}
