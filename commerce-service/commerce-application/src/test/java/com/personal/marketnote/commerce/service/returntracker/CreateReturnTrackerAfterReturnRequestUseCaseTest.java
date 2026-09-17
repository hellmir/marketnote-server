package com.personal.marketnote.commerce.service.returntracker;

import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryCommand;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryPort;
import com.personal.marketnote.commerce.port.out.fulfillment.RegisterFulfillmentReturnDeliveryResult;
import com.personal.marketnote.common.exception.FulfillmentServiceRequestFailedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateReturnTrackerAfterReturnRequestUseCaseTest {
    @Mock
    private RegisterFulfillmentReturnDeliveryPort registerFulfillmentReturnDeliveryPort;
    @Mock
    private ReturnTrackerPersistenceService returnTrackerPersistenceService;

    @InjectMocks
    private CreateReturnTrackerAfterReturnRequestService createReturnTrackerAfterReturnRequestService;

    @Nested
    @DisplayName("반품 입고 추적 데이터 생성 성공")
    class SuccessTest {

        @Test
        @DisplayName("풀필먼트 반�� 등록 성공 시 ReturnTracker를 저장한다")
        void shouldSaveReturnTrackerWhenFulfillmentRegistrationSucceeds() {
            // given
            Long orderId = 1L;
            String returnSlipNumber = "RS-2026041100001";
            RegisterFulfillmentReturnDeliveryCommand command = createCommand(orderId);
            RegisterFulfillmentReturnDeliveryResult result = RegisterFulfillmentReturnDeliveryResult.of(
                    orderId, returnSlipNumber, true, "성공");
            when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(command)).thenReturn(result);

            // when
            createReturnTrackerAfterReturnRequestService.createReturnTracker(command);

            // then
            verify(returnTrackerPersistenceService).saveReturnTracker(orderId, returnSlipNumber);
        }

        @Test
        @DisplayName("풀필먼트 반품 등록 결과의 returnSlipNumber가 ReturnTracker 저장에 전달된다")
        void shouldPassReturnSlipNumberFromResultToTracker() {
            // given
            Long orderId = 1L;
            String returnSlipNumber = "RS-CUSTOM-12345";
            RegisterFulfillmentReturnDeliveryCommand command = createCommand(orderId);
            RegisterFulfillmentReturnDeliveryResult result = RegisterFulfillmentReturnDeliveryResult.of(
                    orderId, returnSlipNumber, true, "��공");
            when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(command)).thenReturn(result);

            // when
            createReturnTrackerAfterReturnRequestService.createReturnTracker(command);

            // then
            verify(returnTrackerPersistenceService).saveReturnTracker(orderId, returnSlipNumber);
            verifyNoMoreInteractions(returnTrackerPersistenceService);
        }
    }

    @Nested
    @DisplayName("풀필먼트 반품 등록 실패")
    class FulfillmentRegistrationFailureTest {

        @Test
        @DisplayName("풀필먼트 반품 등록 실패 시 ReturnTracker를 저장��지 않는다")
        void shouldNotSaveReturnTrackerWhenFulfillmentRegistrationFails() {
            // given
            Long orderId = 1L;
            RegisterFulfillmentReturnDeliveryCommand command = createCommand(orderId);
            when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(command))
                    .thenThrow(new FulfillmentServiceRequestFailedException(new IOException("풀필먼트 서비스 요청 실패")));

            // when
            createReturnTrackerAfterReturnRequestService.createReturnTracker(command);

            // then
            verifyNoInteractions(returnTrackerPersistenceService);
        }
    }

    @Nested
    @DisplayName("ReturnTracker 저장 실패 방어")
    class PersistenceFailureDefenseTest {

        @Test
        @DisplayName("ReturnTracker 저장 시 DataIntegrityViolationException이 발생하면 멱��� 처리한다")
        void shouldHandleIdempotentlyWhenDataIntegrityViolationOccurs() {
            // given
            Long orderId = 1L;
            String returnSlipNumber = "RS-2026041100001";
            RegisterFulfillmentReturnDeliveryCommand command = createCommand(orderId);
            RegisterFulfillmentReturnDeliveryResult result = RegisterFulfillmentReturnDeliveryResult.of(
                    orderId, returnSlipNumber, true, "성공");
            when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(command)).thenReturn(result);
            doThrow(new DataIntegrityViolationException("Unique constraint violation"))
                    .when(returnTrackerPersistenceService).saveReturnTracker(orderId, returnSlipNumber);

            // when & then
            assertThatCode(() -> createReturnTrackerAfterReturnRequestService.createReturnTracker(command))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("ReturnTracker 저��� 시 DataAccessException이 발생하면 예외 없이 로그�� 남긴다")
        void shouldNotPropagateExceptionWhenDataAccessExceptionOccurs() {
            // given
            Long orderId = 1L;
            String returnSlipNumber = "RS-2026041100001";
            RegisterFulfillmentReturnDeliveryCommand command = createCommand(orderId);
            RegisterFulfillmentReturnDeliveryResult result = RegisterFulfillmentReturnDeliveryResult.of(
                    orderId, returnSlipNumber, true, "성공");
            when(registerFulfillmentReturnDeliveryPort.registerReturnDelivery(command)).thenReturn(result);
            doThrow(new DataAccessResourceFailureException("DB connection failed"))
                    .when(returnTrackerPersistenceService).saveReturnTracker(orderId, returnSlipNumber);

            // when & then
            assertThatCode(() -> createReturnTrackerAfterReturnRequestService.createReturnTracker(command))
                    .doesNotThrowAnyException();
        }
    }

    // ==================================================================================
    // 헬퍼 메서드
    // ==================================================================================

    private RegisterFulfillmentReturnDeliveryCommand createCommand(Long orderId) {
        return RegisterFulfillmentReturnDeliveryCommand.builder()
                .orderId(orderId)
                .orderDate("2026-09-17")
                .recipientName("수령인")
                .recipientPhoneNumber("01012345678")
                .recipientAddress("서울시 강남구 테헤란로 123")
                .pickupRecipientName("회수인")
                .pickupRecipientPhoneNumber("01098765432")
                .pickupZipCode("12345")
                .pickupAddress("서울시 서초구")
                .pickupAddressDetail("상세주소")
                .returnReason("단순 변심")
                .returnDetailReason("사이즈 맞지 않음")
                .returnShippingRequest("부재 시 경비실에 맡겨주세요")
                .products(List.of(
                        RegisterFulfillmentReturnDeliveryCommand.ProductItem.of("PROD-001", 1)
                ))
                .build();
    }
}
