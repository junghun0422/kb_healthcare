package com.kb.healthcare.record.dto.request;

import com.kb.healthcare.common.business.record.domain.entity.Record;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class RecordFailEvent {

    private final Long userId;
    private final String recordKey;
    private final Long sourceId;
    private final List<Record> failedRecords;
    private final String errorMessage;

}
