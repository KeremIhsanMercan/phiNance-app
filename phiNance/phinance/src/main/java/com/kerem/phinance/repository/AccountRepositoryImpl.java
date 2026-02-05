package com.kerem.phinance.repository;

import com.kerem.phinance.model.Account;
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
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<Account> findByUserIdAndArchivedFalseCaseInsensitive(String userId, Pageable pageable) {
        Criteria criteria = Criteria.where("userId").is(userId).and("archived").is(false);
        return executePageableQuery(criteria, pageable);
    }

    private Page<Account> executePageableQuery(Criteria criteria, Pageable pageable) {
        Query countQuery = new Query(criteria);
        long total = mongoTemplate.count(countQuery, Account.class);

        Query query = new Query(criteria)
                .with(pageable)
                .collation(Collation.of("en"));

        List<Account> accounts = mongoTemplate.find(query, Account.class);

        return new PageImpl<>(accounts, pageable, total);
    }
}
