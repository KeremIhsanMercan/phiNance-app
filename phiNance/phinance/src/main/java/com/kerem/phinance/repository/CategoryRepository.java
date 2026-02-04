package com.kerem.phinance.repository;

import com.kerem.phinance.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

    List<Category> findByUserIdOrIsDefaultTrue(String userId);

    Page<Category> findByUserIdOrIsDefaultTrue(String userId, Pageable pageable);

    List<Category> findByUserId(String userId);

    List<Category> findByIsDefaultTrue();

    List<Category> findByUserIdAndType(String userId, Category.CategoryType type);

    Optional<Category> findByIdAndUserId(String id, String userId);

    List<Category> findByParentCategoryId(String parentCategoryId);
}
