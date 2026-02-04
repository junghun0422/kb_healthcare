package com.kb.healthcare.common.business.record.service;

import com.kb.healthcare.common.business.record.domain.entity.RecordFail;
import com.kb.healthcare.common.business.record.repository.RecordFailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RequiredArgsConstructor
@Service
public class RecordFailService {

    private final RecordFailRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAll(List<RecordFail> recordFails) { repository.saveAll(recordFails); }

}
