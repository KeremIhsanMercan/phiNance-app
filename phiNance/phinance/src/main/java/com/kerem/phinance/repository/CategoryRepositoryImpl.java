package com.kerem.phinance.repository;

import com.kerem.phinance.model.Category;
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
public class CategoryRepositoryImpl implements CategoryRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Category> findByUserIdOrIsDefaultTrueCaseInsensitive(String userId, Pageable pageable) {
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("userId").is(userId),
                Criteria.where("isDefault").is(true)
        );
        return executePageableQuery(criteria, pageable);
    }

    @Override
    public Page<Category> findByUserIdCaseInsensitive(String userId, Pageable pageable) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return executePageableQuery(criteria, pageable);
    }

    private Page<Category> executePageableQuery(Criteria criteria, Pageable pageable) {
        Query countQuery = new Query(criteria);
        long total = mongoTemplate.count(countQuery, Category.class);

        Query query = new Query(criteria)
                .with(pageable)
                .collation(Collation.of("en"));

        List<Category> categories = mongoTemplate.find(query, Category.class);

        return new PageImpl<>(categories, pageable, total);
    }
}
