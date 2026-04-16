package com.personal.marketnote.commerce.port.out.quickpayment;

/**
 * 빠른결제 배치키 삭제 포트
 *
 * @Author 성효빈
 * @Date 2026-04-16
 * @Description KCP에 배치키 삭제를 요청합니다.
 */
public interface DeleteBatchKeyPort {
    /**
     * @param command 배��키 삭제 요청 정보 {@link DeleteBatchKeyPortCommand}
     * @return 배치키 삭제 결과 {@link DeleteBatchKeyPortResult}
     * @Date 2026-04-16
     * @Author 성효빈
     * @Description KCP에 배치키 삭제를 요청합니다.
     */
    DeleteBatchKeyPortResult deleteBatchKey(DeleteBatchKeyPortCommand command);
}
