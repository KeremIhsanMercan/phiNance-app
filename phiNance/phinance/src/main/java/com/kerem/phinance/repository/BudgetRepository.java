package com.kerem.phinance.repository;

import com.kerem.phinance.model.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends MongoRepository<Budget, String> {

    List<Budget> findByUserIdAndYearAndMonth(String userId, int year, int month);

    Page<Budget> findByUserIdAndYearAndMonth(String userId, int year, int month, Pageable pageable);

    Optional<Budget> findByUserIdAndCategoryIdAndYearAndMonth(
            String userId, String categoryId, int year, int month);

    Optional<Budget> findByIdAndUserId(String id, String userId);

    List<Budget> findByUserId(String userId);

    Page<Budget> findByUserId(String userId, Pageable pageable);

    List<Budget> findByUserIdAndYear(String userId, int year);
}
