package com.kb.healthcare.authserver.service;

import com.kb.healthcare.authserver.dto.request.JoinApiDto;
import com.kb.healthcare.authserver.dto.request.LoginRequestDto;
import com.kb.healthcare.authserver.dto.response.TokenResponseDto;
import com.kb.healthcare.authserver.exception.GlobalException;
import com.kb.healthcare.common.business.user.domain.entity.User;
import com.kb.healthcare.common.business.user.service.UserIdentifierService;
import com.kb.healthcare.common.business.user.service.UserService;
import com.kb.healthcare.common.dto.response.ApiResponse;
import com.kb.healthcare.common.utils.UserEnums;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServerApiService {

    private final UserService userService;
    private final UserIdentifierService userIdentifierService;

    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;


    /**
     * 회원가입
     * [STEP-01] 이메일 중복체크
     * [STEP-02] 비밀번호 암호화
     * [STEP-03] SAVE user
     * @param request
     * @return
     */
    public ApiResponse<Void> joinUser(JoinApiDto request) {
        User exist = userService.findByEmail(request.email());
        if(!Objects.isNull(exist)) {
            throw new GlobalException(
                UserEnums.State.이미_존재하는_이메일.getCode(),
                UserEnums.State.이미_존재하는_이메일.getMessage()
            );
        }

        Set<String> roles = new HashSet<>();
        roles.add("USER");

        User user = User.builder()
                        .name(request.name())
                        .nickName(request.nickName())
                        .email(request.email())
                        .password(passwordEncoder.encode(request.password()))
                        .roles(roles)
                    .build();
        userService.saveUser(user);

        return ApiResponse.<Void>builder().build();
    }

    public ApiResponse<TokenResponseDto> login(LoginRequestDto request) {
        User user = userService.findByEmail(request.email());
        if(Objects.isNull(user)) {
            throw new GlobalException(
                    UserEnums.State.이미_존재하는_이메일.getCode(),
                    UserEnums.State.이미_존재하는_이메일.getMessage()
            );
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new GlobalException(
                UserEnums.State.로그인_실패.getCode(),
                UserEnums.State.로그인_실패.getMessage()
            );
        }

        return ApiResponse.<TokenResponseDto>builder().data(
                TokenResponseDto.builder()
                        .accessToken(generateAccessToken(user))
                        .refreshToken(generateRefreshToken(user))
                        .tokenType("Bearer")
                        .expiresIn(3600L) // 1시간
                        .build()
        ).build();
    }

    public ApiResponse<TokenResponseDto> refresh(String refreshToken) {
        Jwt jwt = jwtDecoder.decode(refreshToken);
        String type = jwt.getClaimAsString("type");
        String userId = jwt.getClaimAsString("userId");

        if(!StringUtils.pathEquals("refresh", type)) {
            throw new GlobalException(
                UserEnums.State.유효하지_않은_토큰.getCode(),
                UserEnums.State.유효하지_않은_토큰.getMessage()
            );
        }

        User user = userService.findById(Long.valueOf(userId)).orElseThrow(() -> new GlobalException(
                UserEnums.State.유효하지_않은_고객.getCode(),
                UserEnums.State.유효하지_않은_고객.getMessage()
        ));

        return ApiResponse.<TokenResponseDto>builder().data(
                TokenResponseDto.builder()
                        .accessToken(generateAccessToken(user))
                        .refreshToken(generateRefreshToken(user))
                        .tokenType("Bearer")
                        .expiresIn(3600L) // 1시간
                        .build()
        ).build();
    }

    private String generateAccessToken(User user) {
        Instant now = Instant.now();
        String scope = "read write";

        String roles = user.getRoles().stream()
                .map(role -> "ROLE_" + role)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:9000")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(user.getEmail())
                .claim("type", "access")
                .claim("scope", scope)
                .claim("roles", roles)
                .claim("nickName", user.getNickName())
                .claim("userId", user.getId())
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    private String generateRefreshToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("http://localhost:9000")
                .issuedAt(now)
                .expiresAt(now.plus(7, ChronoUnit.DAYS))
                .subject(user.getEmail())
                .claim("type", "refresh")
                .claim("userId", user.getId())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}