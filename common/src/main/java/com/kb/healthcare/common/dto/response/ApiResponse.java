package com.kb.healthcare.common.dto.response;

import com.kb.healthcare.common.utils.Constant;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {

    @Builder.Default
    private String resultCode = Constant.요청_성공_코드;

    @Builder.Default
    private String resultMessage = Constant.요청_성공_메시지;

    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> successNoData() {
        return ApiResponse.<T>builder()
                .build();
    }

    public static <T> ApiResponse<T> fail(String resultCode, String resultMessage, T error) {
        return ApiResponse.<T>builder()
                .resultCode(resultCode)
                .resultMessage(resultMessage)
                .data(error)
                .build();
    }

}
