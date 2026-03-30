package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.domain.delivery.FulfillmentDeliveryRegistration;
import com.personal.marketnote.fulfillment.exception.FulfillmentDeliveryAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentDeliveryResult;
import com.personal.marketnote.fulfillment.port.out.delivery.SaveFulfillmentDeliveryRegistrationPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentDeliveryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterFulfillmentDeliveryServiceTest {
    @InjectMocks
    private RegisterFulfillmentDeliveryService registerFulfillmentDeliveryService;

    @Mock
    private RegisterFulfillmentDeliveryPort registerFulfillmentDeliveryPort;

    @Mock
    private SaveFulfillmentDeliveryRegistrationPort saveFulfillmentDeliveryRegistrationPort;

    private RegisterFulfillmentDeliveryCommand buildCommand(String ordNo) {
        RegisterFulfillmentDeliveryGoodsCommand goodsCommand = RegisterFulfillmentDeliveryGoodsCommand.of(
                "PROD001", "20260309", 1
        );
        RegisterFulfillmentDeliveryItemCommand itemCommand = RegisterFulfillmentDeliveryItemCommand.builder()
                .ordNo(ordNo)
                .ordDt("20260309")
                .custNm("홍길동")
                .custTelNo("01012345678")
                .custAddr("서울시 강남구")
                .outWay("01")
                .godCds(List.of(goodsCommand))
                .build();
        return RegisterFulfillmentDeliveryCommand.of("CUST001", "token", List.of(itemCommand));
    }

    private RegisterFulfillmentDeliveryResult buildResult() {
        return RegisterFulfillmentDeliveryResult.of(1, List.of(
                RegisterFulfillmentDeliveryItemResult.of("FMS001", "12345", "등록 성공", "200", null)
        ));
    }

    @Nested
    @DisplayName("registerDelivery (관리자 API용)")
    class RegisterDelivery {
        @Test
        @DisplayName("기존 registerDelivery는 멱등성 체크 없이 Fulfillment API를 호출한다")
        void registerDelivery_callsFulfillmentApiWithoutIdempotencyCheck() {
            // given
            RegisterFulfillmentDeliveryCommand command = buildCommand("12345");
            RegisterFulfillmentDeliveryResult expectedResult = buildResult();
            when(registerFulfillmentDeliveryPort.registerDelivery(any())).thenReturn(expectedResult);

            // when
            RegisterFulfillmentDeliveryResult result = registerFulfillmentDeliveryService.registerDelivery(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            verify(registerFulfillmentDeliveryPort).registerDelivery(any());
            verifyNoInteractions(saveFulfillmentDeliveryRegistrationPort);
        }
    }

    @Nested
    @DisplayName("registerDeliveryIdempotent (Kafka Consumer용)")
    class RegisterDeliveryIdempotent {
        @Test
        @DisplayName("미등록 주문이면 출고 이력을 저장하고 Fulfillment API를 호출한다")
        void registerDeliveryIdempotent_newOrder_savesRegistrationAndCallsApi() {
            // given
            RegisterFulfillmentDeliveryCommand command = buildCommand("12345");
            RegisterFulfillmentDeliveryResult expectedResult = buildResult();
            when(registerFulfillmentDeliveryPort.registerDelivery(any())).thenReturn(expectedResult);

            // when
            RegisterFulfillmentDeliveryResult result = registerFulfillmentDeliveryService.registerDeliveryIdempotent(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);

            InOrder inOrder = inOrder(saveFulfillmentDeliveryRegistrationPort, registerFulfillmentDeliveryPort);
            inOrder.verify(saveFulfillmentDeliveryRegistrationPort).save(any(FulfillmentDeliveryRegistration.class));
            inOrder.verify(registerFulfillmentDeliveryPort).registerDelivery(any());
        }

        @Test
        @DisplayName("이미 출고 요청된 주문이면 FulfillmentDeliveryAlreadyRegisteredException을 던진다")
        void registerDeliveryIdempotent_alreadyRegistered_throwsException() {
            // given
            RegisterFulfillmentDeliveryCommand command = buildCommand("12345");
            doThrow(new FulfillmentDeliveryAlreadyRegisteredException(12345L))
                    .when(saveFulfillmentDeliveryRegistrationPort).save(any(FulfillmentDeliveryRegistration.class));

            // when & then
            assertThatThrownBy(() -> registerFulfillmentDeliveryService.registerDeliveryIdempotent(command))
                    .isInstanceOf(FulfillmentDeliveryAlreadyRegisteredException.class)
                    .hasMessageContaining("orderId=12345");

            verify(saveFulfillmentDeliveryRegistrationPort).save(any(FulfillmentDeliveryRegistration.class));
            verifyNoInteractions(registerFulfillmentDeliveryPort);
        }

        @Test
        @DisplayName("Fulfillment API 호출 실패 시에도 출고 이력은 이미 저장되어 있다")
        void registerDeliveryIdempotent_apiFails_registrationAlreadySaved() {
            // given
            RegisterFulfillmentDeliveryCommand command = buildCommand("12345");
            when(registerFulfillmentDeliveryPort.registerDelivery(any()))
                    .thenThrow(new RuntimeException("Fulfillment API 오류"));

            // when & then
            assertThatThrownBy(() -> registerFulfillmentDeliveryService.registerDeliveryIdempotent(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Fulfillment API 오류");

            verify(saveFulfillmentDeliveryRegistrationPort).save(any(FulfillmentDeliveryRegistration.class));
            verify(registerFulfillmentDeliveryPort).registerDelivery(any());
        }
    }
}
