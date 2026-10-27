package com.personal.marketnote.commerce.port.out.payment.idempotency;

import java.time.LocalDateTime;

public interface DeletePaymentCancelIdempotencyPort {
    /**
     * 지정 시각 이전에 생성된 멱등성 레코드를 삭제한다.
     *
     * @return 삭제된 레코드 수
     */
    int deleteOlderThan(LocalDateTime threshold);
}
