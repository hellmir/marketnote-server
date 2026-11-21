package com.personal.marketnote.common.exception;

/**
 * 벤더 입출력 데이터 검증/디코딩 실패 시 사용하는 추상 부모.
 * IO 통신 실패(네트워크 타임아웃·재시도 등)는 {@link ExternalOperationFailedException}을 사용한다.
 * 전역 핸들러에서 502 BAD_GATEWAY로 매핑된다.
 */
public abstract class VendorOperationFailedException extends RuntimeException {
    protected VendorOperationFailedException(String message) {
        super(message);
    }
}
