package com.personal.marketnote.commerce.port.in.usecase.quickpayment;

import com.personal.marketnote.commerce.port.in.command.quickpayment.IssueBatchKeyCommand;
import com.personal.marketnote.commerce.port.in.result.quickpayment.IssueBatchKeyResult;

/**
 * 빠른결제 배치키 발급 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-04-16
 * @Description KCP 배치키를 발급하고 빠른결제 카드를 저장합니다.
 */
public interface IssueBatchKeyUseCase {
    /**
     * @param command 배치키 발급 커맨드
     * @return 배치키 발급 결과 {@link IssueBatchKeyResult}
     * @Date 2026-04-16
     * @Author 성효빈
     * @Description KCP에 배치키를 발급받고 빠른결제 카드를 DB에 저장합니다.
     */
    IssueBatchKeyResult issueBatchKey(IssueBatchKeyCommand command);
}
