package com.kerem.phinance.repository;

import com.kerem.phinance.model.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BudgetRepositoryCustom {

    Page<Budget> findByUserIdAndYearAndMonthCaseInsensitive(String userId, int year, int month, Pageable pageable);

    Page<Budget> findByUserIdCaseInsensitive(String userId, Pageable pageable);
}
