package com.kb.healthcare.record.controller;

import com.kb.healthcare.record.dto.request.RecordRequestDto;
import com.kb.healthcare.record.service.RecordApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Record API", description = "건강 기록 API")
@RequestMapping("v1/record")
@RequiredArgsConstructor
@RestController
public class RecordController {

    private final RecordApiService service;

    @PreAuthorize("hasAuthority('SCOPE_write') && hasAnyRole('USER')")
    @PostMapping
    @Operation(
        summary = "건강 정보 기록",
        description = "고객의 건강 정보를 기록합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "건강 정보 기록 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @ResponseBody
    public ResponseEntity<com.kb.healthcare.common.dto.response.ApiResponse<Void>> save(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody RecordRequestDto request) {
        return ResponseEntity.ok().body(service.save(jwt, request));
    }

}
