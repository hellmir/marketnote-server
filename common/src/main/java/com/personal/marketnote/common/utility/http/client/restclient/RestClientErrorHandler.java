package com.personal.marketnote.common.utility.http.client.restclient;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;

public class RestClientErrorHandler {

    private RestClientErrorHandler() {
    }

    /**
     * 4xx/5xx 응답 시 예외를 던지지 않는 no-op 에러 핸들러를 반환한다.
     * 호출자는 반드시 응답 상태 코드를 직접 확인하여 에러를 처리해야 한다.
     */
    public static RestClient.ResponseSpec.ErrorHandler noOpErrorHandler() {
        return (request, response) -> {
        };
    }

    public static boolean isError(HttpStatusCode statusCode) {
        return statusCode.is4xxClientError() || statusCode.is5xxServerError();
    }
}
