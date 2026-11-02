package com.personal.marketnote.common.exception;

import java.io.IOException;

public class CommunityServiceRequestFailedException extends ExternalOperationFailedException {
    private static final String COMMUNITY_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE = "커뮤니티 서비스 통신 중 오류가 발생했습니다.";

    public CommunityServiceRequestFailedException(IOException cause) {
        super(String.format(COMMUNITY_SERVICE_REQUEST_FAILED_EXCEPTION_MESSAGE), cause);
    }
}
