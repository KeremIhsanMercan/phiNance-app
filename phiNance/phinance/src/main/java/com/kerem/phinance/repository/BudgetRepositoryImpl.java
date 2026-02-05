package com.kerem.phinance.repository;

import com.kerem.phinance.model.Budget;
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
public class BudgetRepositoryImpl implements BudgetRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Budget> findByUserIdAndYearAndMonthCaseInsensitive(String userId, int year, int month, Pageable pageable) {
        Criteria criteria = Criteria.where("userId").is(userId)
                .and("year").is(year)
                .and("month").is(month);
        return executePageableQuery(criteria, pageable);
    }

    @Override
    public Page<Budget> findByUserIdCaseInsensitive(String userId, Pageable pageable) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return executePageableQuery(criteria, pageable);
    }

    private Page<Budget> executePageableQuery(Criteria criteria, Pageable pageable) {
        Query countQuery = new Query(criteria);
        long total = mongoTemplate.count(countQuery, Budget.class);

        Query query = new Query(criteria)
                .with(pageable)
                .collation(Collation.of("en"));

        List<Budget> budgets = mongoTemplate.find(query, Budget.class);

        return new PageImpl<>(budgets, pageable, total);
    }
}
