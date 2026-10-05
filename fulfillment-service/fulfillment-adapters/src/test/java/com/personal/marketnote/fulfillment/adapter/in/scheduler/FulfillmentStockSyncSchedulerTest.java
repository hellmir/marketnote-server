package com.personal.marketnote.fulfillment.adapter.in.scheduler;

import com.personal.marketnote.fulfillment.configuration.FulfillmentAuthProperties;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFulfillmentAllStockCommand;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.SyncFulfillmentAllStockUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentStockSyncScheduler 테스트")
class FulfillmentStockSyncSchedulerTest {

    @InjectMocks
    private FulfillmentStockSyncScheduler scheduler;

    @Mock
    private SyncFulfillmentAllStockUseCase syncFulfillmentAllStockUseCase;

    @Mock
    private FulfillmentAuthProperties fasstoAuthProperties;

    @Nested
    @DisplayName("성공")
    class Success {

        @Test
        @DisplayName("customerCode가 있으면 전체 재고 동기화를 실행한다")
        void shouldSyncAllWhenCustomerCodeExists() {
            // given
            when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");

            // when
            scheduler.syncAllStocks();

            // then
            verify(syncFulfillmentAllStockUseCase).syncAll(any(SyncFulfillmentAllStockCommand.class));
        }
    }

    @Nested
    @DisplayName("실패")
    class Failure {

        @Test
        @DisplayName("customerCode가 없으면 동기화를 실행하지 않는다")
        void shouldNotSyncWhenCustomerCodeMissing() {
            // given
            when(fasstoAuthProperties.getCustomerCode()).thenReturn(null);

            // when
            scheduler.syncAllStocks();

            // then
            verify(syncFulfillmentAllStockUseCase, never()).syncAll(any());
        }

        @Test
        @DisplayName("동기화 중 예외 발생 시 예외를 전파하지 않는다")
        void shouldNotPropagateExceptionDuringSync() {
            // given
            when(fasstoAuthProperties.getCustomerCode()).thenReturn("CUST001");
            doThrow(new RuntimeException("동기화 실패"))
                    .when(syncFulfillmentAllStockUseCase).syncAll(any(SyncFulfillmentAllStockCommand.class));

            // when & then
            assertThatCode(() -> scheduler.syncAllStocks())
                    .doesNotThrowAnyException();
        }
    }
}
