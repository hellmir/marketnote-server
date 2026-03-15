package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.domain.delivery.FasstoDeliveryRegistration;
import com.personal.marketnote.fulfillment.exception.FasstoDeliveryAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoDeliveryItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoDeliveryResult;
import com.personal.marketnote.fulfillment.port.out.delivery.SaveFasstoDeliveryRegistrationPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFasstoDeliveryPort;
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
class RegisterFasstoDeliveryServiceTest {
    @InjectMocks
    private RegisterFasstoDeliveryService registerFasstoDeliveryService;

    @Mock
    private RegisterFasstoDeliveryPort registerFasstoDeliveryPort;

    @Mock
    private SaveFasstoDeliveryRegistrationPort saveFasstoDeliveryRegistrationPort;

    private RegisterFasstoDeliveryCommand buildCommand(String ordNo) {
        RegisterFasstoDeliveryGoodsCommand goodsCommand = RegisterFasstoDeliveryGoodsCommand.of(
                "PROD001", "20260309", 1
        );
        RegisterFasstoDeliveryItemCommand itemCommand = RegisterFasstoDeliveryItemCommand.builder()
                .ordNo(ordNo)
                .ordDt("20260309")
                .custNm("홍길동")
                .custTelNo("01012345678")
                .custAddr("서울시 강남구")
                .outWay("01")
                .godCds(List.of(goodsCommand))
                .build();
        return RegisterFasstoDeliveryCommand.of("CUST001", "token", List.of(itemCommand));
    }

    private RegisterFasstoDeliveryResult buildResult() {
        return RegisterFasstoDeliveryResult.of(1, List.of(
                RegisterFasstoDeliveryItemResult.of("FMS001", "12345", "등록 성공", "200", null)
        ));
    }

    @Nested
    @DisplayName("registerDelivery (관리자 API용)")
    class RegisterDelivery {
        @Test
        @DisplayName("기존 registerDelivery는 멱등성 체크 없이 Fassto API를 호출한다")
        void registerDelivery_callsFasstoApiWithoutIdempotencyCheck() {
            // given
            RegisterFasstoDeliveryCommand command = buildCommand("12345");
            RegisterFasstoDeliveryResult expectedResult = buildResult();
            when(registerFasstoDeliveryPort.registerDelivery(any())).thenReturn(expectedResult);

            // when
            RegisterFasstoDeliveryResult result = registerFasstoDeliveryService.registerDelivery(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            verify(registerFasstoDeliveryPort).registerDelivery(any());
            verifyNoInteractions(saveFasstoDeliveryRegistrationPort);
        }
    }

    @Nested
    @DisplayName("registerDeliveryIdempotent (Kafka Consumer용)")
    class RegisterDeliveryIdempotent {
        @Test
        @DisplayName("미등록 주문이면 출고 이력을 저장하고 Fassto API를 호출한다")
        void registerDeliveryIdempotent_newOrder_savesRegistrationAndCallsApi() {
            // given
            RegisterFasstoDeliveryCommand command = buildCommand("12345");
            RegisterFasstoDeliveryResult expectedResult = buildResult();
            when(registerFasstoDeliveryPort.registerDelivery(any())).thenReturn(expectedResult);

            // when
            RegisterFasstoDeliveryResult result = registerFasstoDeliveryService.registerDeliveryIdempotent(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);

            InOrder inOrder = inOrder(saveFasstoDeliveryRegistrationPort, registerFasstoDeliveryPort);
            inOrder.verify(saveFasstoDeliveryRegistrationPort).save(any(FasstoDeliveryRegistration.class));
            inOrder.verify(registerFasstoDeliveryPort).registerDelivery(any());
        }

        @Test
        @DisplayName("이미 출고 요청된 주문이면 FasstoDeliveryAlreadyRegisteredException을 던진다")
        void registerDeliveryIdempotent_alreadyRegistered_throwsException() {
            // given
            RegisterFasstoDeliveryCommand command = buildCommand("12345");
            doThrow(new FasstoDeliveryAlreadyRegisteredException(12345L))
                    .when(saveFasstoDeliveryRegistrationPort).save(any(FasstoDeliveryRegistration.class));

            // when & then
            assertThatThrownBy(() -> registerFasstoDeliveryService.registerDeliveryIdempotent(command))
                    .isInstanceOf(FasstoDeliveryAlreadyRegisteredException.class)
                    .hasMessageContaining("orderId=12345");

            verify(saveFasstoDeliveryRegistrationPort).save(any(FasstoDeliveryRegistration.class));
            verifyNoInteractions(registerFasstoDeliveryPort);
        }

        @Test
        @DisplayName("Fassto API 호출 실패 시에도 출고 이력은 이미 저장되어 있다")
        void registerDeliveryIdempotent_apiFails_registrationAlreadySaved() {
            // given
            RegisterFasstoDeliveryCommand command = buildCommand("12345");
            when(registerFasstoDeliveryPort.registerDelivery(any()))
                    .thenThrow(new RuntimeException("Fassto API 오류"));

            // when & then
            assertThatThrownBy(() -> registerFasstoDeliveryService.registerDeliveryIdempotent(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Fassto API 오류");

            verify(saveFasstoDeliveryRegistrationPort).save(any(FasstoDeliveryRegistration.class));
            verify(registerFasstoDeliveryPort).registerDelivery(any());
        }
    }
}
