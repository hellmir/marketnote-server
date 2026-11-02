package com.personal.marketnote.common.adapter.in.api.format;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class BaseResponse<T> {
    private int statusCode;
    private String code;
    private LocalDateTime timestamp;
    private T content;
    private String message;

    public static <T> BaseResponse<T> of(T content, HttpStatus status, String code, String message) {
        return of(content, status.value(), code, message);
    }

    public static <T> BaseResponse<T> of(T content, int statusCode, String code, String message) {
        return new BaseResponse<>(statusCode, code, LocalDateTime.now(), content, message);
    }

    public static <T> BaseResponse<T> of(T content, int statusCode, String code) {
        return of(content, statusCode, code, null);
    }

    public static <T> BaseResponse<T> of(T content, HttpStatus status, String code) {
        return of(content, status, code, null);
    }

    public static <T> BaseResponse<T> of(int statusCode, String code) {
        return of(null, statusCode, code);
    }

    public static <T> BaseResponse<T> of(HttpStatus status, String code) {
        return of(null, status, code);
    }

    public static <T> BaseResponse<T> of(int statusCode, String code, String message) {
        return of(null, statusCode, code, message);
    }

    public static <T> BaseResponse<T> of(HttpStatus status, String code, String message) {
        return of(null, status, code, message);
    }
}
