package com.kb.healthcare.authserver.controller;

import com.kb.healthcare.common.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping("v1/check")
@RestController
public class AuthserverController {

    @PreAuthorize("hasAuthority('SCOPE_read') && hasAnyRole('USER')")
    @GetMapping
    public ResponseEntity<ApiResponse<Void>> check(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok().body(
            ApiResponse.<Void>builder().build()
        );
    }

}
