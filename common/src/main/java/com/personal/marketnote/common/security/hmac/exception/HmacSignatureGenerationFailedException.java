package com.personal.marketnote.common.security.hmac.exception;

public class HmacSignatureGenerationFailedException extends RuntimeException {
    public HmacSignatureGenerationFailedException(Throwable cause) {
        super("HMAC 서명 생성에 실패했습니다.", cause);
    }
}
