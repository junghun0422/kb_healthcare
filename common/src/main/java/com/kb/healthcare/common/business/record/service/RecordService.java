package com.kb.healthcare.common.business.record.service;

import com.kb.healthcare.common.business.record.domain.entity.Record;
import com.kb.healthcare.common.business.record.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class RecordService {

    private final RecordRepository repository;

    public void save(Record record) { repository.save(record); }

    public void saveAll(List<Record> records) { repository.saveAll(records); }

}
