package com.kerem.phinance.repository;

import com.kerem.phinance.model.Goal;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends MongoRepository<Goal, String> {
    
    List<Goal> findByUserId(String userId);
    
    List<Goal> findByUserIdAndCompletedFalse(String userId);
    
    Optional<Goal> findByIdAndUserId(String id, String userId);
    
    List<Goal> findByDependencyGoalIdsContaining(String goalId);
}
