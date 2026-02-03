package com.kb.healthcare.common.business.record.repository;

import com.kb.healthcare.common.business.record.domain.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

}
