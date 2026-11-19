package com.personal.marketnote.common.exception;

public class InvalidSwaggerOriginException extends IllegalArgumentException {
    public InvalidSwaggerOriginException(String origin) {
        super("ERR_SWAGGER_01::유효하지 않은 Swagger origin 값입니다. value=" + origin);
    }

    public InvalidSwaggerOriginException(String origin, Throwable cause) {
        super("ERR_SWAGGER_01::유효하지 않은 Swagger origin 값입니다. value=" + origin, cause);
    }
}
