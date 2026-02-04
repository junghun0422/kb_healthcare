package com.kb.healthcare.common.business.record.repository;

import com.kb.healthcare.common.business.record.domain.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceRepository extends JpaRepository<Source, Long> {

    Source findByRecordKeyAndMode(String recordKey, int mode);

}
