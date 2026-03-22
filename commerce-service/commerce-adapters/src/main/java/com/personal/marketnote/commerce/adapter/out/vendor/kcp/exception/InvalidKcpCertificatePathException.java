package com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception;

public class InvalidKcpCertificatePathException extends IllegalStateException {
    public InvalidKcpCertificatePathException(String path) {
        super("잘못된 인증서 경로입니다: " + path);
    }
}
