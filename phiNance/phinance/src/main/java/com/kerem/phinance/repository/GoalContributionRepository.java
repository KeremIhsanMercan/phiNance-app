package com.kerem.phinance.repository;

import com.kerem.phinance.model.GoalContribution;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoalContributionRepository extends MongoRepository<GoalContribution, String> {
    
    List<GoalContribution> findByGoalId(String goalId);
    
    List<GoalContribution> findByUserId(String userId);
}
