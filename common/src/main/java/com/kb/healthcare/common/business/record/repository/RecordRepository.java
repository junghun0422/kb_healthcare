package com.kb.healthcare.common.business.record.repository;

import com.kb.healthcare.common.business.record.domain.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<Record, Long>, RecordRepositoryCustom {

}
