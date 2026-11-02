package com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception;

public class KcpCertificateLoadFailedException extends IllegalStateException {
    public KcpCertificateLoadFailedException(String certInfoPath, Throwable cause) {
        super("KCP 인증서 파일 로드 실패: " + certInfoPath, cause);
    }
}
