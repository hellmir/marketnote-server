package com.personal.marketnote.commerce.port.out.payment.vendor;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Builder;

@Builder
public record PaymentApprovalVendorResult(
        String resCd,
        String resMsg,
        String resEnMsg,
        String tno,
        String amount,
        String payMethod,
        String cardCd,
        String cardName,
        String cardNo,
        String appNo,
        String appTime,
        String noinf,
        String noinfType,
        String quota,
        String cardMny,
        String couponMny,
        String partcancYn,
        String cardBinType01,
        String cardBinType02,
        String rawResponse
) {
    public boolean isSuccess() {
        return FormatValidator.equals(resCd, "0000");
    }
}
