package com.kerem.phinance.repository;

import com.kerem.phinance.model.SavedFilter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedFilterRepository extends MongoRepository<SavedFilter, String> {
    
    List<SavedFilter> findByUserId(String userId);
    
    Optional<SavedFilter> findByIdAndUserId(String id, String userId);
}
