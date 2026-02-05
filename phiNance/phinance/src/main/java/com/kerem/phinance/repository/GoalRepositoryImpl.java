package com.kerem.phinance.repository;

import com.kerem.phinance.model.Goal;
import org.springframework.data.mongodb.core.query.Collation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@RequiredArgsConstructor
public class GoalRepositoryImpl implements GoalRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Goal> findByUserIdCaseInsensitive(String userId, Pageable pageable) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return executePageableQuery(criteria, pageable);
    }

    private Page<Goal> executePageableQuery(Criteria criteria, Pageable pageable) {
        Query countQuery = new Query(criteria);
        long total = mongoTemplate.count(countQuery, Goal.class);

        Query query = new Query(criteria)
                .with(pageable)
                .collation(Collation.of("en"));

        List<Goal> goals = mongoTemplate.find(query, Goal.class);

        return new PageImpl<>(goals, pageable, total);
    }
}
