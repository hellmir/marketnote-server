package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryPort;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryResult;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
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
        if (result == null) {
            return;
        }

        persistReturnTracker(command.orderId(), result.returnSlipNumber());
    }

    private RegisterFulfillmentReturnDeliveryResult registerReturnDelivery(RegisterFulfillmentReturnDeliveryCommand command) {
        try {
            return registerFulfillmentReturnDeliveryPort.registerReturnDelivery(command);
        } catch (FulfillmentServiceRequestFailedException e) {
            log.error("풀필먼트 반품 등록 실패 - orderId: {}, error: {}",
                    command.orderId(), e.getMessage(), e);
            return null;
        }
    }

    private void persistReturnTracker(Long orderId, String returnSlipNumber) {
        try {
            returnTrackerPersistenceService.saveReturnTracker(orderId, returnSlipNumber);
        } catch (DataIntegrityViolationException e) {
            log.warn("ReturnTracker 이미 존재 - orderId: {} (멱등 처리)", orderId);
        } catch (DataAccessException e) {
            log.error("ReturnTracker 저장 실패 - orderId: {}, returnSlipNumber: {}, error: {}",
                    orderId, returnSlipNumber, e.getMessage(), e);
        }
    }
}
