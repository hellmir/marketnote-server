package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFulfillmentStockCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentStocksResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentStockDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentStocksUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import com.personal.marketnote.fulfillment.port.out.commerce.UpdateCommerceInventoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("풀필먼트 재고 동기화 테스트")
class SyncFulfillmentStockUseCaseTest {
    @InjectMocks
    private SyncFulfillmentStockService service;
    @Mock
    private RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;
    @Mock
    private GetFulfillmentStockDetailUseCase getFulfillmentStockDetailUseCase;
    @Mock
    private GetFulfillmentStocksUseCase getFulfillmentStocksUseCase;
    @Mock
    private UpdateCommerceInventoryPort updateCommerceInventoryPort;

    @Nested
    @DisplayName("sync 성공")
    class SyncSuccess {
        @Test
        @DisplayName("재고 동기화를 수행하고 커머스 재고를 업데이트한다")
        void shouldSyncAndUpdateInventory() {
            // given
            FulfillmentAccessToken accessToken = FulfillmentAccessToken.of("token", "20270101120000");
            when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(accessToken);
            when(getFulfillmentStockDetailUseCase.getStockDetail(any()))
                    .thenReturn(GetFulfillmentStocksResult.of(0, List.of()));
            SyncFulfillmentStockCommand command = SyncFulfillmentStockCommand.of("CUST001", List.of(1L));
            // when
            service.sync(command);
            // then
            verify(updateCommerceInventoryPort).updateInventories(any());
        }
    }

    @Nested
    @DisplayName("sync 실패")
    class SyncFailure {
        @Test
        @DisplayName("command가 null이면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowWhenCommandIsNull() {
            assertThatThrownBy(() -> service.sync(null))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("customerCode가 없으면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowWhenCustomerCodeIsNull() {
            SyncFulfillmentStockCommand command = SyncFulfillmentStockCommand.of(null, List.of(1L));
            assertThatThrownBy(() -> service.sync(command))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("productIds가 없으면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowWhenProductIdsIsNull() {
            SyncFulfillmentStockCommand command = SyncFulfillmentStockCommand.of("CUST001", null);
            assertThatThrownBy(() -> service.sync(command))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }
    }
}
