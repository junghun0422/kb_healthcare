package com.kb.healthcare.authserver.controller;

import com.kb.healthcare.authserver.dto.request.JoinApiDto;
import com.kb.healthcare.authserver.dto.request.LoginRequestDto;
import com.kb.healthcare.authserver.dto.response.TokenResponseDto;
import com.kb.healthcare.authserver.service.AuthServerApiService;
import com.kb.healthcare.common.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("v1/user")
@RequiredArgsConstructor
@RestController
public class AccountController {

    private final AuthServerApiService service;

    @PostMapping("join")
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> joinUser(@Validated @RequestBody JoinApiDto request) {
        return ResponseEntity.ok(service.joinUser(request));
    }

    @PostMapping("login")
    @ResponseBody
    public ResponseEntity<ApiResponse<TokenResponseDto>> login(@Validated @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(service.login(request));
    }

    @PostMapping("refresh")
    @ResponseBody
    public ResponseEntity<ApiResponse<TokenResponseDto>> refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(service.refresh(refreshToken));
    }

}
