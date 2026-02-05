package com.kerem.phinance.repository;

import com.kerem.phinance.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountRepositoryCustom {

    Page<Account> findByUserIdAndArchivedFalseCaseInsensitive(String userId, Pageable pageable);
}
