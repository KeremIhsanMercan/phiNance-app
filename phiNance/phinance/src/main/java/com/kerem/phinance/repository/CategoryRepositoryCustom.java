package com.kerem.phinance.repository;

import com.kerem.phinance.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryRepositoryCustom {

    Page<Category> findByUserIdOrIsDefaultTrueCaseInsensitive(String userId, Pageable pageable);

    Page<Category> findByUserIdCaseInsensitive(String userId, Pageable pageable);
}
