package com.kb.healthcare.common.business.record.service;

import com.kb.healthcare.common.business.record.domain.entity.Source;
import com.kb.healthcare.common.business.record.repository.SourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SourceService {

    private final SourceRepository repository;

    public Source findByUserIdAndRecordKeyAndMode(Long userId, String recordKey, int mode) {
        return repository.findByUserIdAndRecordKeyAndMode(userId, recordKey, mode);
    }

    public Source save(Source source) { return repository.save(source); }


}
