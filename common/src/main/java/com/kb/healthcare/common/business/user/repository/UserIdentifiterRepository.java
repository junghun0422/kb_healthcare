package com.kb.healthcare.common.business.user.repository;

import com.kb.healthcare.common.business.user.domain.entity.UserIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIdentifiterRepository extends JpaRepository<UserIdentifier, Long> {
    UserIdentifier findByUserIdAndRecordKey(Long userId, String recordKey);

}
