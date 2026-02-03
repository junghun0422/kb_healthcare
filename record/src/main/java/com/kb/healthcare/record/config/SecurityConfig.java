package com.kb.healthcare.record.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(resourceServer ->
                        resourceServer.jwt(jwt -> jwt
                            .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .formLogin(form -> form.disable());

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("scope");
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix("SCOPE_");

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

}