package com.kerem.phinance.repository;

import com.kerem.phinance.model.FavoriteFilter;
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
public class FavoriteFilterRepositoryImpl implements FavoriteFilterRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<FavoriteFilter> findByUserIdCaseInsensitive(String userId, Pageable pageable) {
        Criteria criteria = Criteria.where("userId").is(userId);
        return executePageableQuery(criteria, pageable);
    }

    private Page<FavoriteFilter> executePageableQuery(Criteria criteria, Pageable pageable) {
        Query countQuery = new Query(criteria);
        long total = mongoTemplate.count(countQuery, FavoriteFilter.class);

        Query query = new Query(criteria)
                .with(pageable)
                .collation(Collation.of("en"));

        List<FavoriteFilter> filters = mongoTemplate.find(query, FavoriteFilter.class);

        return new PageImpl<>(filters, pageable, total);
    }
}
