package com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception;

public class KcpSignatureGenerationFailedException extends IllegalStateException {
    public KcpSignatureGenerationFailedException(String tno, Throwable cause) {
        super("KCP 서명 생성 실패: tno=" + tno, cause);
    }
}
