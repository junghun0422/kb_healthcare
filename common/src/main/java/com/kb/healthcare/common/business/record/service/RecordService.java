package com.kb.healthcare.common.business.record.service;

import com.kb.healthcare.common.business.record.domain.entity.Record;
import com.kb.healthcare.common.business.record.dto.FindRecordResDto;import com.kb.healthcare.common.business.record.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RecordService {

    private final RecordRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAllInNewTransaction(List<Record> records) { repository.saveAll(records); }

    public List<FindRecordResDto> findByRecordsByUserId(Long userId) { return repository.findByRecordsByUserId(userId); }

}
