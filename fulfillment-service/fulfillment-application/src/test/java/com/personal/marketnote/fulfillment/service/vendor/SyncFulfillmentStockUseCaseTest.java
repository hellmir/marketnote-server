package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.kafka.event.FulfillmentInventorySyncedEvent;
import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStocksCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFulfillmentAllStockCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFulfillmentStockCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentStockInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentStocksResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentStockDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentStocksUseCase;
import com.personal.marketnote.fulfillment.port.out.event.PublishFulfillmentInventorySyncedEventPort;
import com.personal.marketnote.fulfillment.utility.FulfillmentAuthenticatedCaller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("풀필먼트 재고 동기화 테스트")
class SyncFulfillmentStockUseCaseTest {
    @InjectMocks
    private SyncFulfillmentStockService service;

    @Mock
    private FulfillmentAuthenticatedCaller fulfillmentAuthenticatedCaller;
    @Mock
    private GetFulfillmentStockDetailUseCase getFulfillmentStockDetailUseCase;
    @Mock
    private GetFulfillmentStocksUseCase getFulfillmentStocksUseCase;
    @Mock
    private PublishFulfillmentInventorySyncedEventPort publishFulfillmentInventorySyncedEventPort;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void stubAuthenticatedCaller() {
        lenient().when(fulfillmentAuthenticatedCaller.call(any())).thenAnswer(invocation -> {
            Function<String, Object> operation = invocation.getArgument(0);
            return operation.apply("token");
        });
    }

    @Nested
    @DisplayName("sync 성공")
    class SyncSuccess {
        @Test
        @DisplayName("재고 동기화를 수행하고 커머스 재고를 업데이트한다")
        void shouldSyncAndUpdateInventory() {
            // given
            when(getFulfillmentStockDetailUseCase.getStockDetail(any(GetFulfillmentStockDetailCommand.class)))
                    .thenReturn(GetFulfillmentStocksResult.of(0, List.of()));
            SyncFulfillmentStockCommand command = SyncFulfillmentStockCommand.of("CUST001", List.of(1L));

            // when
            service.sync(command);

            // then
            verify(publishFulfillmentInventorySyncedEventPort).publish(any());
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

    @Nested
    @DisplayName("syncAll 성공")
    class SyncAllSuccess {
        @Test
        @DisplayName("전체 재고 동기화를 수행하고 커머스 재고를 업데이트한다")
        void shouldSyncAllAndUpdateInventory() {
            // given

            FulfillmentStockInfoResult stockInfo = FulfillmentStockInfoResult.of(
                    "WH001", "GOD001", "1", "상품명", null, null, null,
                    10, 0, 10, null, null, null, List.of(), null
            );
            when(getFulfillmentStocksUseCase.getStocks(any(GetFulfillmentStocksCommand.class)))
                    .thenReturn(GetFulfillmentStocksResult.of(1, List.of(stockInfo)));
            SyncFulfillmentAllStockCommand command = SyncFulfillmentAllStockCommand.of("CUST001");

            // when
            service.syncAll(command);

            // then
            verify(publishFulfillmentInventorySyncedEventPort).publish(any());
        }
    }

    @Nested
    @DisplayName("syncAll 실패")
    class SyncAllFailure {
        @Test
        @DisplayName("command가 null이면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowWhenCommandIsNull() {
            assertThatThrownBy(() -> service.syncAll(null))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("customerCode가 없으면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowWhenCustomerCodeIsNull() {
            SyncFulfillmentAllStockCommand command = SyncFulfillmentAllStockCommand.of(null);
            assertThatThrownBy(() -> service.syncAll(command))
                    .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }
    }

    // ==================================================================================
    // productKey 전환 (#4073)
    // ==================================================================================

    @Nested
    @DisplayName("sync productKey 송신")
    class SyncProductKey {
        @Test
        @DisplayName("productKeys가 있으면 Fassto에 productKey.toString()이 customerProductCode로 전달된다")
        void shouldSendProductKeyToFasstoWhenProductKeysPresent() {
            // given
            UUID productKey = UUID.randomUUID();
            when(getFulfillmentStockDetailUseCase.getStockDetail(any(GetFulfillmentStockDetailCommand.class)))
                    .thenReturn(GetFulfillmentStocksResult.of(0, List.of()));
            SyncFulfillmentStockCommand command =
                    SyncFulfillmentStockCommand.ofProductKeys("CUST001", List.of(productKey));

            // when
            service.sync(command);

            // then
            ArgumentCaptor<GetFulfillmentStockDetailCommand> captor =
                    ArgumentCaptor.forClass(GetFulfillmentStockDetailCommand.class);
            verify(getFulfillmentStockDetailUseCase).getStockDetail(captor.capture());
            assertThat(captor.getValue().customerProductCode()).isEqualTo(productKey.toString());
            assertThat(captor.getValue().productKey()).isEqualTo(productKey);
        }

        @Test
        @DisplayName("productKeys 경로에서 발행되는 InventoryItem에 productKey가 담긴다")
        void shouldPublishProductKeyInInventoryItem() {
            // given
            UUID productKey = UUID.randomUUID();
            FulfillmentStockInfoResult stockInfo = FulfillmentStockInfoResult.of(
                    "WH001", "GOD001", productKey.toString(), "상품명", null, null, null,
                    5, 0, 5, null, null, null, List.of(), null
            );
            when(getFulfillmentStockDetailUseCase.getStockDetail(any(GetFulfillmentStockDetailCommand.class)))
                    .thenReturn(GetFulfillmentStocksResult.of(1, List.of(stockInfo)));
            SyncFulfillmentStockCommand command =
                    SyncFulfillmentStockCommand.ofProductKeys("CUST001", List.of(productKey));

            // when
            service.sync(command);

            // then
            ArgumentCaptor<FulfillmentInventorySyncedEvent> captor =
                    ArgumentCaptor.forClass(FulfillmentInventorySyncedEvent.class);
            verify(publishFulfillmentInventorySyncedEventPort).publish(captor.capture());
            List<FulfillmentInventorySyncedEvent.InventoryItem> items = captor.getValue().inventories();
            assertThat(items).hasSize(1);
            assertThat(items.get(0).productKey()).isEqualTo(productKey);
            assertThat(items.get(0).productId()).isNull();
            assertThat(items.get(0).stock()).isEqualTo(5);
        }

        @Test
        @DisplayName("productKeys가 없으면 productIds fallback으로 Fassto에 productId가 전달된다")
        void shouldFallbackToProductIdWhenProductKeysAbsent() {
            // given
            when(getFulfillmentStockDetailUseCase.getStockDetail(any(GetFulfillmentStockDetailCommand.class)))
                    .thenReturn(GetFulfillmentStocksResult.of(0, List.of()));
            SyncFulfillmentStockCommand command = SyncFulfillmentStockCommand.of("CUST001", List.of(42L));

            // when
            service.sync(command);

            // then
            ArgumentCaptor<GetFulfillmentStockDetailCommand> captor =
                    ArgumentCaptor.forClass(GetFulfillmentStockDetailCommand.class);
            verify(getFulfillmentStockDetailUseCase).getStockDetail(captor.capture());
            assertThat(captor.getValue().customerProductCode()).isEqualTo("42");
            assertThat(captor.getValue().productKey()).isNull();
        }

        @Test
        @DisplayName("productIds 경로에서 발행되는 InventoryItem에 productId가 담기고 productKey는 null이다")
        void shouldPublishProductIdInInventoryItemWhenProductIdsPath() {
            // given
            FulfillmentStockInfoResult stockInfo = FulfillmentStockInfoResult.of(
                    "WH001", "GOD001", "1", "상품명", null, null, null,
                    7, 0, 7, null, null, null, List.of(), null
            );
            when(getFulfillmentStockDetailUseCase.getStockDetail(any(GetFulfillmentStockDetailCommand.class)))
                    .thenReturn(GetFulfillmentStocksResult.of(1, List.of(stockInfo)));
            SyncFulfillmentStockCommand command = SyncFulfillmentStockCommand.of("CUST001", List.of(1L));

            // when
            service.sync(command);

            // then
            ArgumentCaptor<FulfillmentInventorySyncedEvent> captor =
                    ArgumentCaptor.forClass(FulfillmentInventorySyncedEvent.class);
            verify(publishFulfillmentInventorySyncedEventPort).publish(captor.capture());
            FulfillmentInventorySyncedEvent.InventoryItem item = captor.getValue().inventories().get(0);
            assertThat(item.productId()).isEqualTo(1L);
            assertThat(item.productKey()).isNull();
        }
    }

    @Nested
    @DisplayName("syncAll customerProductCode 역파싱")
    class SyncAllParsing {
        @Test
        @DisplayName("customerProductCode가 UUID 포맷이면 이벤트 productKey에 담긴다")
        void shouldParseUuidAndPublishProductKey() {
            // given
            UUID productKey = UUID.randomUUID();
            FulfillmentStockInfoResult stockInfo = FulfillmentStockInfoResult.of(
                    "WH001", "GOD001", productKey.toString(), "상품명", null, null, null,
                    3, 0, 3, null, null, null, List.of(), null
            );
            when(getFulfillmentStocksUseCase.getStocks(any(GetFulfillmentStocksCommand.class)))
                    .thenReturn(GetFulfillmentStocksResult.of(1, List.of(stockInfo)));
            SyncFulfillmentAllStockCommand command = SyncFulfillmentAllStockCommand.of("CUST001");

            // when
            service.syncAll(command);

            // then
            ArgumentCaptor<FulfillmentInventorySyncedEvent> captor =
                    ArgumentCaptor.forClass(FulfillmentInventorySyncedEvent.class);
            verify(publishFulfillmentInventorySyncedEventPort).publish(captor.capture());
            FulfillmentInventorySyncedEvent.InventoryItem item = captor.getValue().inventories().get(0);
            assertThat(item.productKey()).isEqualTo(productKey);
            assertThat(item.productId()).isNull();
            assertThat(item.stock()).isEqualTo(3);
        }

        @Test
        @DisplayName("customerProductCode가 숫자 포맷이면 Long.parseLong fallback으로 productId에 담긴다")
        void shouldParseLegacyLongAndPublishProductId() {
            // given
            FulfillmentStockInfoResult stockInfo = FulfillmentStockInfoResult.of(
                    "WH001", "GOD001", "42", "상품명", null, null, null,
                    9, 0, 9, null, null, null, List.of(), null
            );
            when(getFulfillmentStocksUseCase.getStocks(any(GetFulfillmentStocksCommand.class)))
                    .thenReturn(GetFulfillmentStocksResult.of(1, List.of(stockInfo)));
            SyncFulfillmentAllStockCommand command = SyncFulfillmentAllStockCommand.of("CUST001");

            // when
            service.syncAll(command);

            // then
            ArgumentCaptor<FulfillmentInventorySyncedEvent> captor =
                    ArgumentCaptor.forClass(FulfillmentInventorySyncedEvent.class);
            verify(publishFulfillmentInventorySyncedEventPort).publish(captor.capture());
            FulfillmentInventorySyncedEvent.InventoryItem item = captor.getValue().inventories().get(0);
            assertThat(item.productId()).isEqualTo(42L);
            assertThat(item.productKey()).isNull();
        }

        @Test
        @DisplayName("customerProductCode가 UUID/숫자 둘 다 아니면 해당 항목을 skip하고 이벤트를 발행하지 않는다")
        void shouldSkipWhenParsingFails() {
            // given
            FulfillmentStockInfoResult stockInfo = FulfillmentStockInfoResult.of(
                    "WH001", "GOD001", "INVALID-CODE", "상품명", null, null, null,
                    10, 0, 10, null, null, null, List.of(), null
            );
            when(getFulfillmentStocksUseCase.getStocks(any(GetFulfillmentStocksCommand.class)))
                    .thenReturn(GetFulfillmentStocksResult.of(1, List.of(stockInfo)));
            SyncFulfillmentAllStockCommand command = SyncFulfillmentAllStockCommand.of("CUST001");

            // when
            service.syncAll(command);

            // then
            verify(publishFulfillmentInventorySyncedEventPort, org.mockito.Mockito.never()).publish(any());
        }
    }
}
