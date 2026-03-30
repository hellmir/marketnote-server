package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.exception.FulfillmentGoodsAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentGoodsItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.out.goods.SaveFulfillmentGoodsRegistrationPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentGoodsPort;
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
class RegisterFulfillmentGoodsServiceTest {
    @InjectMocks
    private RegisterFulfillmentGoodsService registerFulfillmentGoodsService;

    @Mock
    private RegisterFulfillmentGoodsPort registerFulfillmentGoodsPort;

    @Mock
    private SaveFulfillmentGoodsRegistrationPort saveFulfillmentGoodsRegistrationPort;

    private RegisterFulfillmentGoodsCommand buildCommand(String productId) {
        RegisterFulfillmentGoodsItemCommand itemCommand = RegisterFulfillmentGoodsItemCommand.builder()
                .cstGodCd(productId)
                .godNm("테스트 상품")
                .godType("1")
                .giftDiv("01")
                .build();
        return RegisterFulfillmentGoodsCommand.of("CUST001", "token", List.of(itemCommand));
    }

    private RegisterFulfillmentGoodsResult buildResult() {
        return RegisterFulfillmentGoodsResult.of(1, List.of(
                RegisterFulfillmentGoodsItemResult.of("등록 성공", "200", "10")
        ));
    }

    @Nested
    @DisplayName("registerGoods (관리자 API용)")
    class RegisterGoods {
        @Test
        @DisplayName("기존 registerGoods는 멱등성 체크 없이 Fulfillment API를 호출한다")
        void registerGoods_callsFulfillmentApiWithoutIdempotencyCheck() {
            // given
            RegisterFulfillmentGoodsCommand command = buildCommand("10");
            RegisterFulfillmentGoodsResult expectedResult = buildResult();
            when(registerFulfillmentGoodsPort.registerGoods(any())).thenReturn(expectedResult);

            // when
            RegisterFulfillmentGoodsResult result = registerFulfillmentGoodsService.registerGoods(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            verify(registerFulfillmentGoodsPort).registerGoods(any());
            verifyNoInteractions(saveFulfillmentGoodsRegistrationPort);
        }
    }

    @Nested
    @DisplayName("registerGoodsIdempotent (Kafka Consumer용)")
    class RegisterGoodsIdempotent {
        @Test
        @DisplayName("상품이 미등록 상태이면 등록 이력을 저장하고 Fulfillment API를 호출한다")
        void registerGoodsIdempotent_newProduct_savesRegistrationAndCallsApi() {
            // given
            RegisterFulfillmentGoodsCommand command = buildCommand("10");
            RegisterFulfillmentGoodsResult expectedResult = buildResult();
            when(registerFulfillmentGoodsPort.registerGoods(any())).thenReturn(expectedResult);

            // when
            RegisterFulfillmentGoodsResult result = registerFulfillmentGoodsService.registerGoodsIdempotent(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);

            InOrder inOrder = inOrder(saveFulfillmentGoodsRegistrationPort, registerFulfillmentGoodsPort);
            inOrder.verify(saveFulfillmentGoodsRegistrationPort).save(any());
            inOrder.verify(registerFulfillmentGoodsPort).registerGoods(any());
        }

        @Test
        @DisplayName("이미 등록된 상품이면 FulfillmentGoodsAlreadyRegisteredException을 던진다")
        void registerGoodsIdempotent_alreadyRegistered_throwsException() {
            // given
            RegisterFulfillmentGoodsCommand command = buildCommand("10");
            doThrow(new FulfillmentGoodsAlreadyRegisteredException(10L))
                    .when(saveFulfillmentGoodsRegistrationPort).save(any());

            // when & then
            assertThatThrownBy(() -> registerFulfillmentGoodsService.registerGoodsIdempotent(command))
                    .isInstanceOf(FulfillmentGoodsAlreadyRegisteredException.class)
                    .hasMessageContaining("productId=10");

            verify(saveFulfillmentGoodsRegistrationPort).save(any());
            verifyNoInteractions(registerFulfillmentGoodsPort);
        }

        @Test
        @DisplayName("Fulfillment API 호출 실패 시에도 등록 이력은 이미 저장되어 있다")
        void registerGoodsIdempotent_apiFails_registrationAlreadySaved() {
            // given
            RegisterFulfillmentGoodsCommand command = buildCommand("10");
            when(registerFulfillmentGoodsPort.registerGoods(any()))
                    .thenThrow(new RuntimeException("Fulfillment API 오류"));

            // when & then
            assertThatThrownBy(() -> registerFulfillmentGoodsService.registerGoodsIdempotent(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Fulfillment API 오류");

            verify(saveFulfillmentGoodsRegistrationPort).save(any());
            verify(registerFulfillmentGoodsPort).registerGoods(any());
        }
    }
}
