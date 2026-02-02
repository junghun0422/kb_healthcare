package com.kb.healthcare.authserver.config.spring;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.*;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    /**
     * ============================================================================
     * OAuth2 Authorization Server 보안 설정 (Order 1)
     * ============================================================================
     * 역할: OAuth2/OIDC 표준 프로토콜 엔드포인트 보호
     * 처리 경로: /oauth2/*, /.well-known/*
     * 사용 대상: 외부 클라이언트 애플리케이션이 이 서버와 OAuth2 통신할 때
     *
     * 예시:
     * - POST /oauth2/token          → 토큰 발급
     * - GET  /oauth2/authorize      → 인가 코드 요청
     * - POST /oauth2/introspect     → 토큰 검증
     * - POST /oauth2/revoke         → 토큰 폐기
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // OAuth2 Authorization Server의 기본 보안 설정 적용
        // 토큰 엔드포인트, 인가 엔드포인트 등 자동 구성
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        // OAuth2 Authorization Server 설정 가져오기
        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                // OpenID Connect 1.0 지원 활성화
                // - UserInfo 엔드포인트, Discovery 엔드포인트
                .oidc(Customizer.withDefaults());
        http
            // 예외 처리 설정.
            .exceptionHandling(exceptions -> exceptions
                    // 인증되지 않은 사용자가 접근 시 처리
                    .defaultAuthenticationEntryPointFor(
                            // HTML 요청인 경우 /login 페이지로 리다이렉트
                            new LoginUrlAuthenticationEntryPoint("/login"),
                            // HTML 미디어 타입 요청에만 적용
                            new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                    )
            )
            // OAuth2 Resource Server 설정 (토큰 기반 인증)
            .oauth2ResourceServer(resourceServer -> resourceServer
                    // JWT 기반 인증 활성화
                    // Access Token이 Bearer 토큰으로 전달되면 JWT 검증
                    .jwt(Customizer.withDefaults())
            );
        return http.build();
    }

    /**
     * ============================================================================
     * 일반 애플리케이션 보안 설정 (Order 2)
     * ============================================================================
     * 역할: 비즈니스 API 및 리소스 보호
     * 처리 경로: /v1/*, /api/* 등 (OAuth2 표준 경로 제외한 모든 경로)
     * 사용 대상: 우리 서비스를 이용하는 최종 사용자
     *
     * 예시:
     * - POST /v1/auth/login         → 로그인 (JWT 발급)
     * - POST /v1/auth/join          → 회원가입
     * - GET  /api/user/profile      → 사용자 프로필 조회 (JWT 인증 필요)
     * - GET  /v1/check/check        → 권한 확인 (JWT 인증 필요)
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF(Cross-Site Request Forgery) 보호 비활성화
            // REST API는 stateless이므로 CSRF 토큰 불필요
            // (세션을 사용하지 않으므로 CSRF 공격 위험 없음)
            .csrf(csrf -> csrf.disable())
            // HTTP 요청에 대한 인가 규칙 설정
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/v1/user/**").permitAll()
                .anyRequest().authenticated()
            )
            // OAuth2 Resource Server로 동작 (JWT 검증)
            .oauth2ResourceServer(resourceServer ->
                resourceServer.jwt(jwt -> jwt
                    // JWT를 Spring Security의 Authenticatino 객체로 변환
                   .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            )
            // Form 기반 로그인 비활성화
            .formLogin(form -> form.disable());

        return http.build();
    }

    /**
     * ============================================================================
     * JWT 인증 변환기 (JWT → Spring Security Authentication)
     * ============================================================================
     * 역할: JWT의 claims를 Spring Security의 권한(GrantedAuthority)으로 변환
     *
     * JWT 구조:
     * {
     *   "sub": "user@example.com",      → Principal (주체)
     *   "scope": "read write",           → SCOPE_read, SCOPE_write
     *   "roles": "ROLE_USER,ROLE_ADMIN", → ROLE_USER, ROLE_ADMIN
     *   "userId": 1,
     *   ...
     * }
     *
     * 변환 결과:
     * Authentication {
     *   principal: "user@example.com",
     *   authorities: [SCOPE_read, SCOPE_write, ROLE_USER, ROLE_ADMIN]
     * }
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // JWT의 scope claim을 권한으로 변환하는 컨버터
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // scope claim의 이름 지정 (JWT의 "scope" 필드를 읽음)
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("SCOPE_");

        // JWT를 Authentication으로 변환하는 메인 컨버터
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        // scope + roles 를 모두 권한으로 변환
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // 1. scope 처리
            Collection<GrantedAuthority> authorities = new ArrayList<>(
                jwtGrantedAuthoritiesConverter.convert(jwt)
            );

            // 2. roles 처리
            String roles = jwt.getClaimAsString("roles");
            if(Objects.nonNull(roles)) {
                // "ROLE_USER,ROLE_ADMIN" 형식 또는 단일 "ROLE_USER"
                Arrays.stream(roles.split(" "))
                        .map(String::trim)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
            return authorities;
        });
        return jwtAuthenticationConverter;
    }

    /**
     * ============================================================================
     * 클라이언트 등록 정보 (OAuth2 Client 설정)
     * ============================================================================
     * 역할: 이 Authorization Server를 사용할 클라이언트 앱 등록
     *
     * 용도:
     * - 외부 앱이 이 서버를 통해 사용자 인증/인가를 받을 때 사용
     * - 예: 모바일 앱, 웹 앱, 제3자 서비스
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                // 클라이언트 ID (공개 정보)
                .clientId("kb-healthcare-client")
                // 클라이언트 Secret (비밀 정보, 암호화 저장)
                .clientSecret(passwordEncoder().encode("secret"))
                // 클라이언트 인증 방법
                // - CLIENT_SECRET_BASIC: HTTP Basic 인증 (Authorizationn 헤더)
                // - CLIENT_SECRET_POST: POST body에 포함
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                // OAuth2 Grant Types (인가 방식)
                // - AUTHORIZATION_CODE: 인가 코드 방식 (가장 안전, 웹/모바일 앱 권장)
                // - REFRESH_TOKEN: Access Token 갱신
                // - CLIENT_CREDENTIALS: 서버 간 통신용 (사용자 없이 앱 자체 인증)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                // 인가 완료 후 리다이렉트한 URI
                // 인가 코드를 받을 클라이언트 앱의 콜백 주소
                .redirectUri("http://localhost:9000/login/oauth2/code/kb-healthcare-client")
                .redirectUri("http://localhost:9000/authorized")
                // OAuth2 Scope (접근 범위)
                .scope("read")
                .scope("write")
                // 토큰 설정
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))
                        .refreshTokenTimeToLive(Duration.ofDays(7))
                        .build())
                .build();
        // 메모리 기반 저장소 (실제 운영 환경에서는 DB 사용 권장)
        return new InMemoryRegisteredClientRepository(registeredClient);
    }

    /**
     * ============================================================================
     * JWT 서명용 키 소스 (JWK Set)
     * ============================================================================
     * 역할: JWT 토큰 생성/검증에 사용할 RSA 키 쌍 제공
     *
     * - Private Key: JWT 서명 (토큰 발급 시 사용)
     * - Public Key: JWT 검증 (토큰 검증 시 사용, 공개 가능)
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        // RSA 키 쌍 생성
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // RSA 키를 JWK(JSON Web Key) 형식으로 변환
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                // 키 ID (여러 키를 구분하기 위한 식별자)
                .keyID(UUID.randomUUID().toString())
                .build();

        // JWK Set 생성 (여러 키를 담을 수 있는 컨테이너)
        JWKSet jwkSet = new JWKSet(rsaKey);

        // Immutable(불변) JWK Source 반환
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * RSA 키 쌍 생성
     */
    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    /**
     * ============================================================================
     * JWT 디코더 (JWT 검증용)
     * ============================================================================
     * 역할: Access Token을 검증하고 파싱
     *
     * - 서명 검증 (Public Key 사용)
     * - 만료 시간 검증
     * - Issuer 검증 등
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) { return new NimbusJwtEncoder(jwkSource); }

    /**
     * ============================================================================
     * JWT 인코더 (JWT 생성용)
     * ============================================================================
     * 역할: Access Token 생성
     *
     * - JWT claims 설정
     * - Private Key로 서명
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) { return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource); }

    /**
     * ============================================================================
     * Authorization Server 설정
     * ============================================================================
     * 역할: Authorization Server의 메타데이터 설정
     *
     * - Issuer URL 설정 (JWT의 iss claim에 사용)
     */
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:9000")
                .jwkSetEndpoint("/oauth2/jwks")
                .build();
    }

    /**
     * ============================================================================
     * 비밀번호 인코더
     * ============================================================================
     * 역할: 비밀번호 암호화/검증
     *
     * - BCrypt 알고리즘 사용 (단방향 해시)
     * - 회원가입 시 비밀번호 암호화
     * - 로그인 시 비밀번호 검증
     */
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

}