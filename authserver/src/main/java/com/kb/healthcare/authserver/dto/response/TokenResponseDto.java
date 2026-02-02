package com.kb.healthcare.authserver.dto.response;

import lombok.Builder;

public record TokenResponseDto(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long expiresIn
) {

    @Builder
    public TokenResponseDto(String accessToken, String refreshToken, String tokenType, Long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
    }

}
