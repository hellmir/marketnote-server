package com.personal.marketnote.commerce.service.payment;

import com.personal.marketnote.commerce.port.in.result.payment.GetUnknownPaymentEventsResult;
import com.personal.marketnote.commerce.port.in.usecase.payment.GetUnknownPaymentEventsUseCase;
import com.personal.marketnote.commerce.port.out.payment.FindPspPaymentEventPort;
import com.personal.marketnote.common.application.UseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetUnknownPaymentEventsService implements GetUnknownPaymentEventsUseCase {
    private final FindPspPaymentEventPort findPspPaymentEventPort;

    @Override
    public List<GetUnknownPaymentEventsResult> getUnknownPaymentEvents() {
        return findPspPaymentEventPort.findAllByUnknownStatus().stream()
                .map(GetUnknownPaymentEventsResult::from)
                .toList();
    }
}
