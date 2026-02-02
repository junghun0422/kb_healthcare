package com.kb.healthcare.record.controller;

import com.kb.healthcare.record.service.RecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RequestMapping("v1/record")
@RequiredArgsConstructor
@RestController
public class RecordController {

    private final RecordService service;

    @PreAuthorize("hasAuthority('SCOPE_read') && hasAnyRole('USER')")
    @PostMapping
    public ResponseEntity<com.kb.healthcare.common.dto.response.ApiResponse<Void>> save() {
        return ResponseEntity.ok().body(service.save());
    }

}
