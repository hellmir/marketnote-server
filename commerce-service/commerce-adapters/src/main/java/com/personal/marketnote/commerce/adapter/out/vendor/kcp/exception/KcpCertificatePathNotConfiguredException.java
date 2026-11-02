package com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception;

public class KcpCertificatePathNotConfiguredException extends IllegalStateException {
    public KcpCertificatePathNotConfiguredException() {
        super("KCP 인증서 경로(kcp.cert-info-path)가 설정되지 않았습니다");
    }
}
