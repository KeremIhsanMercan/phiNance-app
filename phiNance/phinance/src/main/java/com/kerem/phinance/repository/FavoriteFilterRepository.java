package com.kerem.phinance.repository;

import com.kerem.phinance.model.FavoriteFilter;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteFilterRepository extends MongoRepository<FavoriteFilter, String> {

    List<FavoriteFilter> findByUserId(String userId);

    Optional<FavoriteFilter> findByIdAndUserId(String id, String userId);

    void deleteByIdAndUserId(String id, String userId);
}
