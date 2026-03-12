package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.exception.FasstoGoodsAlreadyRegisteredException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFasstoGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoGoodsItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFasstoGoodsResult;
import com.personal.marketnote.fulfillment.port.out.goods.SaveFasstoGoodsRegistrationPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFasstoGoodsPort;
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
class RegisterFasstoGoodsServiceTest {
    @InjectMocks
    private RegisterFasstoGoodsService registerFasstoGoodsService;

    @Mock
    private RegisterFasstoGoodsPort registerFasstoGoodsPort;

    @Mock
    private SaveFasstoGoodsRegistrationPort saveFasstoGoodsRegistrationPort;

    private RegisterFasstoGoodsCommand buildCommand(String productId) {
        RegisterFasstoGoodsItemCommand itemCommand = RegisterFasstoGoodsItemCommand.of(
                productId, "테스트 상품", "1", "01",
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null,
                null, null, null, null
        );
        return RegisterFasstoGoodsCommand.of("CUST001", "token", List.of(itemCommand));
    }

    private RegisterFasstoGoodsResult buildResult() {
        return RegisterFasstoGoodsResult.of(1, List.of(
                RegisterFasstoGoodsItemResult.of("등록 성공", "200", "10")
        ));
    }

    @Nested
    @DisplayName("registerGoods (관리자 API용)")
    class RegisterGoods {
        @Test
        @DisplayName("기존 registerGoods는 멱등성 체크 없이 Fassto API를 호출한다")
        void registerGoods_callsFasstoApiWithoutIdempotencyCheck() {
            // given
            RegisterFasstoGoodsCommand command = buildCommand("10");
            RegisterFasstoGoodsResult expectedResult = buildResult();
            when(registerFasstoGoodsPort.registerGoods(any())).thenReturn(expectedResult);

            // when
            RegisterFasstoGoodsResult result = registerFasstoGoodsService.registerGoods(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);
            verify(registerFasstoGoodsPort).registerGoods(any());
            verifyNoInteractions(saveFasstoGoodsRegistrationPort);
        }
    }

    @Nested
    @DisplayName("registerGoodsIdempotent (Kafka Consumer용)")
    class RegisterGoodsIdempotent {
        @Test
        @DisplayName("상품이 미등록 상태이면 등록 이력을 저장하고 Fassto API를 호출한다")
        void registerGoodsIdempotent_newProduct_savesRegistrationAndCallsApi() {
            // given
            RegisterFasstoGoodsCommand command = buildCommand("10");
            RegisterFasstoGoodsResult expectedResult = buildResult();
            when(registerFasstoGoodsPort.registerGoods(any())).thenReturn(expectedResult);

            // when
            RegisterFasstoGoodsResult result = registerFasstoGoodsService.registerGoodsIdempotent(command);

            // then
            assertThat(result.dataCount()).isEqualTo(1);

            InOrder inOrder = inOrder(saveFasstoGoodsRegistrationPort, registerFasstoGoodsPort);
            inOrder.verify(saveFasstoGoodsRegistrationPort).save(any());
            inOrder.verify(registerFasstoGoodsPort).registerGoods(any());
        }

        @Test
        @DisplayName("이미 등록된 상품이면 FasstoGoodsAlreadyRegisteredException을 던진다")
        void registerGoodsIdempotent_alreadyRegistered_throwsException() {
            // given
            RegisterFasstoGoodsCommand command = buildCommand("10");
            doThrow(new FasstoGoodsAlreadyRegisteredException(10L))
                    .when(saveFasstoGoodsRegistrationPort).save(any());

            // when & then
            assertThatThrownBy(() -> registerFasstoGoodsService.registerGoodsIdempotent(command))
                    .isInstanceOf(FasstoGoodsAlreadyRegisteredException.class)
                    .hasMessageContaining("productId=10");

            verify(saveFasstoGoodsRegistrationPort).save(any());
            verifyNoInteractions(registerFasstoGoodsPort);
        }

        @Test
        @DisplayName("Fassto API 호출 실패 시에도 등록 이력은 이미 저장되어 있다")
        void registerGoodsIdempotent_apiFails_registrationAlreadySaved() {
            // given
            RegisterFasstoGoodsCommand command = buildCommand("10");
            when(registerFasstoGoodsPort.registerGoods(any()))
                    .thenThrow(new RuntimeException("Fassto API 오류"));

            // when & then
            assertThatThrownBy(() -> registerFasstoGoodsService.registerGoodsIdempotent(command))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Fassto API 오류");

            verify(saveFasstoGoodsRegistrationPort).save(any());
            verify(registerFasstoGoodsPort).registerGoods(any());
        }
    }
}
