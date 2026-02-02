package com.kb.healthcare.common.business.user.service;

import com.kb.healthcare.common.business.user.repository.UserIdentifiterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserIdentifierService {

    private final UserIdentifiterRepository repository;
}
