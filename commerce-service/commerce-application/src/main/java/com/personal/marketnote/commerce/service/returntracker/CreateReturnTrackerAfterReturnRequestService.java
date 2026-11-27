package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryPort;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryResult;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateReturnTrackerAfterReturnRequestService {
    private final RegisterFulfillmentReturnDeliveryPort registerFulfillmentReturnDeliveryPort;
    private final ReturnTrackerPersistenceService returnTrackerPersistenceService;

    public void createReturnTracker(RegisterFulfillmentReturnDeliveryCommand command) {
        RegisterFulfillmentReturnDeliveryResult result = registerReturnDelivery(command);
        if (FormatValidator.hasNoValue(result)) {
            return;
        }

        persistReturnTracker(command.orderId(), result.returnSlipNumber());
    }

    private RegisterFulfillmentReturnDeliveryResult registerReturnDelivery(
        RegisterFulfillmentReturnDeliveryCommand command
    ) {
        try {
            return registerFulfillmentReturnDeliveryPort.registerReturnDelivery(command);
        } catch (FulfillmentServiceRequestFailedException fsrfe) {
            log.warn("fallback applied: 풀필먼트 반품 등록 실패 — null 반환 (ReturnTracker 미생성). orderId={}",
                command.orderId(), fsrfe
            );
            return null;
        }
    }

    private void persistReturnTracker(Long orderId, String returnSlipNumber) {
        try {
            returnTrackerPersistenceService.saveReturnTracker(orderId, returnSlipNumber);
        } catch (DataIntegrityViolationException ignored) {
            // 동시 처리로 인한 멱등 저장 충돌 — 동일 orderId ReturnTracker 이미 존재 (멱등 처리)
            log.info("ReturnTracker 이미 존재 - orderId: {} (멱등 처리)", orderId);
        } catch (DataAccessException dae) {
            log.error("ReturnTracker 저장 실패. orderId={}, returnSlipNumber={}",
                orderId, returnSlipNumber, dae
            );
        }
    }
}
