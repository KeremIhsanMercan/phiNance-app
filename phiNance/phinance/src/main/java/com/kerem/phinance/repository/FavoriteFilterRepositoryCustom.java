package com.kerem.phinance.repository;

import com.kerem.phinance.model.FavoriteFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoriteFilterRepositoryCustom {

    Page<FavoriteFilter> findByUserIdCaseInsensitive(String userId, Pageable pageable);
}
