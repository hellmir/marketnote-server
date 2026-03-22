package com.personal.marketnote.commerce.adapter.out.vendor.kcp.exception;

public class KcpPrivateKeyPathNotConfiguredException extends IllegalStateException {
    public KcpPrivateKeyPathNotConfiguredException() {
        super("KCP 개인키 경로(kcp.private-key-path)가 설정되지 않았습니다");
    }
}
