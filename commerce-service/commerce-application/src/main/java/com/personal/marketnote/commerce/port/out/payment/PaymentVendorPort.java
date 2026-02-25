package com.personal.marketnote.commerce.port.out.payment;

import com.personal.marketnote.commerce.port.out.payment.vendor.*;

public interface PaymentVendorPort {
    /**
     * @return 결제 대행사 사이트 코드 {@link String}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 결제 대행사 사이트 코드를 조회합니다.
     */
    String getVendorSiteCd();

    /**
     * @param command 거래 등록 요청 정보 {@link TradeRegisterVendorCommand}
     * @return 거래 등록 결과 {@link TradeRegisterVendorResult}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 결제 대행사에 거래를 등록합니다.
     */
    TradeRegisterVendorResult registerTrade(TradeRegisterVendorCommand command);

    /**
     * @param command 결제 승인 요청 정보 {@link PaymentApprovalVendorCommand}
     * @return 결제 승인 결과 {@link PaymentApprovalVendorResult}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 결제 대행사에 결제 승인을 요청합니다.
     */
    PaymentApprovalVendorResult approvePayment(PaymentApprovalVendorCommand command);

    /**
     * @param command 결제 취소 요청 정보 {@link PaymentCancelVendorCommand}
     * @return 결제 취소 결과 {@link PaymentCancelVendorResult}
     * @Date 2026-02-25
     * @Author 성효빈
     * @Description 결제 대행사에 결제 취소를 요청합니다.
     */
    PaymentCancelVendorResult cancelPayment(PaymentCancelVendorCommand command);
}
