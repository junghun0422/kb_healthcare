package com.kb.healthcare.common.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class UserEnums {

    @Getter
    @RequiredArgsConstructor
    public enum State {

        이미_존재하는_이메일("USER-FAIL-0001", "이미 가입된 이메일 정보입니다."),
        로그인_실패("USER-FAIL-0002", "이메일 또는 비밀번호를 확인해주세요."),
        유효하지_않은_토큰("USER-FAIL-0003", "유효하지 않은 토큰정보입니다."),
        유효하지_않은_고객("USER-FAIL_0004", "유효하지 않은 회원정보입니다")
        ;

        private final String code;
        private final String message;
    }

}
