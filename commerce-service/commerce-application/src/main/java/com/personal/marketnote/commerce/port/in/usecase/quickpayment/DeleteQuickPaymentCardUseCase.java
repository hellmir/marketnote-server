package com.personal.marketnote.commerce.port.in.usecase.quickpayment;

import com.personal.marketnote.commerce.port.in.command.quickpayment.DeleteQuickPaymentCardCommand;

/**
 * 빠른결제 카드 ��제 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-16
 * @Description KCP 배치키 삭제 후 빠른결제 카드를 비활성화합니다.
 */
public interface DeleteQuickPaymentCardUseCase {
    /**
     * @param command 빠른���제 카드 삭제 커맨드
     * @Date 2026-04-16
     * @Author 성효빈
     * @Description KCP에 배치키 삭제 요청 후 DB에서 카드를 비활성화합니다.
     */
    void delete(DeleteQuickPaymentCardCommand command);
}
