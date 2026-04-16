package com.personal.marketnote.commerce.port.out.quickpayment;

/**
 * 빠른결제 배치키 발급 포트
 *
 * @Author 성효빈
 * @Date 2026-04-16
 * @Description KCP에 배치키 발급을 요청합니다.
 */
public interface IssueBatchKeyPort {
    /**
     * @param command 배치키 발급 요청 정보 {@link IssueBatchKeyPortCommand}
     * @return 배치키 발급 결과 {@link IssueBatchKeyPortResult}
     * @Date 2026-04-16
     * @Author 성효빈
     * @Description KCP에 배치키 발급을 요청합니다.
     */
    IssueBatchKeyPortResult issueBatchKey(IssueBatchKeyPortCommand command);
}
