package com.kerem.phinance.repository;

import com.kerem.phinance.model.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    
    List<Account> findByUserIdAndArchivedFalse(String userId);
    
    List<Account> findByUserId(String userId);
    
    Optional<Account> findByIdAndUserId(String id, String userId);
    
    boolean existsByIdAndUserId(String id, String userId);
}
