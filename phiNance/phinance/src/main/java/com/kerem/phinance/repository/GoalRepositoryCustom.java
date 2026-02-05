package com.kerem.phinance.repository;

import com.kerem.phinance.model.Goal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GoalRepositoryCustom {

    Page<Goal> findByUserIdCaseInsensitive(String userId, Pageable pageable);
}
