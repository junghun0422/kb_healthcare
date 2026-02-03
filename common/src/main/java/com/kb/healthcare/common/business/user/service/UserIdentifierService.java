package com.kb.healthcare.common.business.user.service;

import com.kb.healthcare.common.business.user.domain.entity.UserIdentifier;
import com.kb.healthcare.common.business.user.repository.UserIdentifiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserIdentifierService {

    private final UserIdentifiterRepository repository;

    public UserIdentifier findByUserIdAndRecordKey(Long userId, String recordKey) { return repository.findByUserIdAndRecordKey(userId, recordKey); }

    public void save(UserIdentifier userIdentifier) { repository.save(userIdentifier); }

}
