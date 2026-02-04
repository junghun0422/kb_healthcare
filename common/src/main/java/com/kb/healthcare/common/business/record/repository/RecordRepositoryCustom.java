package com.kb.healthcare.common.business.record.repository;

import com.kb.healthcare.common.business.record.dto.FindRecordResDto;
import com.kb.healthcare.common.business.user.domain.entity.User;
import java.util.List;

public interface RecordRepositoryCustom {

    List<FindRecordResDto> findByRecordsByUserId(Long userId);

}
