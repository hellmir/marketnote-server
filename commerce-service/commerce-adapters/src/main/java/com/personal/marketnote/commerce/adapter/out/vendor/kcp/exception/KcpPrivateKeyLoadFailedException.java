package com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception;

public class KcpPrivateKeyLoadFailedException extends IllegalStateException {
    public KcpPrivateKeyLoadFailedException(String privateKeyPath, Throwable cause) {
        super("KCP 개인키 파일 로드 실패: " + privateKeyPath, cause);
    }
}
