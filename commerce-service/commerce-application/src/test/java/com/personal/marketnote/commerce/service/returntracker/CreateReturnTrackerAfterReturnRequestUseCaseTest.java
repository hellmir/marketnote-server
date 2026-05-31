package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryPort;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryResult;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateReturnTrackerAfterReturnRequestService 테스트")
class CreateReturnTrackerAfterReturnRequestUseCaseTest {
    @InjectMocks
    private CreateReturnTrackerAfterReturnRequestService createReturnTrackerAfterReturnRequestService;

    @Mock
    private RegisterFulfillmentReturnDeliveryPort registerFulfillmentReturnDeliveryPort;

    @Mock
    private ReturnTrackerPersistenceService returnTrackerPersistenceService;

    @Test
    @DisplayName("풀필먼트 반품 등록 성공 시 ReturnTrackerPersistenceService를 호출한다")
    void shouldCallPersistenceServiceWhenRegistrationSucceeds() {
        // given
        RegisterFulfillmentReturnDeliveryCommand command = createCommand(100L);
        RegisterFulfillmentReturnDeliveryResult result = RegisterFulfillmentReturnDeliveryResult.of(
                100L, "RTN-SLIP-001", true, "성공"
        );
        when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(any())).thenReturn(result);

        // when
        createReturnTrackerAfterReturnRequestService.createReturnTracker(command);

        // then
        verify(returnTrackerPersistenceService).saveReturnTracker(100L, "RTN-SLIP-001");
    }

    @Test
    @DisplayName("풀필먼트 반품 등록에서 returnSlipNumber가 ReturnTrackerPersistenceService에 전달된다")
    void shouldPassReturnSlipNumberToPersistenceService() {
        // given
        RegisterFulfillmentReturnDeliveryCommand command = createCommand(200L);
        RegisterFulfillmentReturnDeliveryResult result = RegisterFulfillmentReturnDeliveryResult.of(
                200L, "RTN-SLIP-999", true, "성공"
        );
        when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(any())).thenReturn(result);

        // when
        createReturnTrackerAfterReturnRequestService.createReturnTracker(command);

        // then
        verify(returnTrackerPersistenceService).saveReturnTracker(200L, "RTN-SLIP-999");
    }

    @Test
    @DisplayName("풀필먼트 반품 등록 실패 시 ReturnTrackerPersistenceService를 호출하지 않는다")
    void shouldNotCallPersistenceServiceWhenRegistrationFails() {
        // given
        RegisterFulfillmentReturnDeliveryCommand command = createCommand(100L);
        when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(any()))
                .thenThrow(new FulfillmentServiceRequestFailedException(new IOException("풀필먼트 호출 실패")));

        // when
        createReturnTrackerAfterReturnRequestService.createReturnTracker(command);

        // then
        verifyNoInteractions(returnTrackerPersistenceService);
    }

    @Test
    @DisplayName("풀필먼트 반품 등록 실패 시 예외가 전파되지 않는다")
    void shouldNotPropagateExceptionWhenRegistrationFails() {
        // given
        RegisterFulfillmentReturnDeliveryCommand command = createCommand(100L);
        when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(any()))
                .thenThrow(new FulfillmentServiceRequestFailedException(new IOException("풀필먼트 호출 실패")));

        // when & then (예외 전파 없음)
        createReturnTrackerAfterReturnRequestService.createReturnTracker(command);
    }

    private RegisterFulfillmentReturnDeliveryCommand createCommand(Long orderId) {
        return RegisterFulfillmentReturnDeliveryCommand.builder()
                .orderId(orderId)
                .orderDate("20260409")
                .recipientName("수령인")
                .recipientPhoneNumber("01012345678")
                .recipientAddress("서울시 강남구")
                .pickupRecipientName("회수 수령인")
                .pickupRecipientPhoneNumber("01099998888")
                .pickupZipCode("06234")
                .pickupAddress("회수지 주소")
                .pickupAddressDetail("회수지 상세")
                .returnReason("단순 변심")
                .returnDetailReason("상세 사유")
                .returnShippingRequest("부재시 경비실")
                .products(List.of(
                        RegisterFulfillmentReturnDeliveryCommand.ProductItem.of("100", 2)
                ))
                .build();
    }
}
