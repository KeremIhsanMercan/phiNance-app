package com.kerem.phinance.repository;

import com.kerem.phinance.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    
    Page<Transaction> findByUserId(String userId, Pageable pageable);
    
    List<Transaction> findByUserIdAndAccountId(String userId, String accountId);
    
    Optional<Transaction> findByIdAndUserId(String id, String userId);
    
    List<Transaction> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    
    List<Transaction> findByUserIdAndCategoryId(String userId, String categoryId);
    
    @Query("{ 'userId': ?0, 'date': { $gte: ?1, $lte: ?2 }, 'type': ?3 }")
    List<Transaction> findByUserIdAndDateBetweenAndType(
            String userId, 
            LocalDate startDate, 
            LocalDate endDate, 
            Transaction.TransactionType type);
    
    @Query("{ 'userId': ?0, 'categoryId': ?1, 'date': { $gte: ?2, $lte: ?3 } }")
    List<Transaction> findByUserIdAndCategoryIdAndDateBetween(
            String userId, 
            String categoryId, 
            LocalDate startDate, 
            LocalDate endDate);
    
    List<Transaction> findByRecurringTrueAndDate(LocalDate date);
    
    void deleteByIdAndUserId(String id, String userId);
}
