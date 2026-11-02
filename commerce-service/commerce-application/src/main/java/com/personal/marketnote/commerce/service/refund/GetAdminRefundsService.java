package com.personal.marketnote.commerce.service.refund;

import com.personal.marketnote.commerce.domain.refund.Refund;
import com.personal.marketnote.commerce.port.in.result.refund.GetAdminRefundResult;
import com.personal.marketnote.commerce.port.in.usecase.refund.GetAdminRefundsUseCase;
import com.personal.marketnote.commerce.port.out.refund.FindRefundPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

/**
 * 관리자 환불 목록 조회 서비스.
 *
 * @author 성효빈
 * @since 2026-03-02
 */
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetAdminRefundsService implements GetAdminRefundsUseCase {
    private final FindRefundPort findRefundPort;

    @Override
    public List<GetAdminRefundResult> getRefundsByOrderId(Long orderId) {
        List<Refund> refunds = findRefundPort.findByOrderId(orderId);
        return refunds.stream()
                .map(GetAdminRefundResult::from)
                .toList();
    }
}
