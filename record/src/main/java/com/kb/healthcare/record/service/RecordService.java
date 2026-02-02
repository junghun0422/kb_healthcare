package com.kb.healthcare.record.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RecordService {

    public com.kb.healthcare.common.dto.response.ApiResponse<Void> save() {
        log.error(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        return com.kb.healthcare.common.dto.response.ApiResponse.<Void>builder().build();
    }
}
