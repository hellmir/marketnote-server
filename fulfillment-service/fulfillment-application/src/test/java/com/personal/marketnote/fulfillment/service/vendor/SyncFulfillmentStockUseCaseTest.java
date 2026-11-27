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
import com.personal.marketnote.fulfillment.port.out.goods.FindFulfillmentGoodsRegistrationPort;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("풀필먼트 재고 동기화 테스트")
class SyncFulfillmentStockUseCaseTest {
    private static final String AUTO_GEN_FASSTO_CODE = "0000000000000000000001";
    private static final String IMPORT_FASSTO_CODE = "VZL00001";

    @InjectMocks
    private SyncFulfillmentStockService service;

    @Mock
    private FulfillmentAuthenticatedCaller fulfillmentAuthenticatedCaller;
    @Mock
    private GetFulfillmentStockDetailUseCase getFulfillmentStockDetailUseCase;
    @Mock
    private GetFulfillmentStocksUseCase getFulfillmentStocksUseCase;
    @Mock
    private FindFulfillmentGoodsRegistrationPort findFulfillmentGoodsRegistrationPort;
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

    private FulfillmentStockInfoResult buildStockInfo(String customerProductCode, int availableStock) {
        return FulfillmentStockInfoResult.builder()
            .warehouseCode("WH001")
            .productCode("GOD001")
            .customerProductCode(customerProductCode)
            .productName("상품명")
            .stockQuantity(availableStock)
            .defectiveStockQuantity(0)
            .availableStockQuantity(availableStock)
            .goodsSerialNumbers(List.of())
            .build();
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
            SyncFulfillmentStockCommand command =
                SyncFulfillmentStockCommand.of(null, List.of(AUTO_GEN_FASSTO_CODE));
            assertThatThrownBy(() -> service.sync(command))
                .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("fasstoProductCodes가 없으면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowWhenFasstoProductCodesIsNull() {
            SyncFulfillmentStockCommand command = SyncFulfillmentStockCommand.of("CUST001", null);
            assertThatThrownBy(() -> service.sync(command))
                .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }

        @Test
        @DisplayName("fasstoProductCodes가 모두 blank이면 FulfillmentQueryParameterNoValueException이 발생한다")
        void shouldThrowWhenAllFasstoProductCodesAreBlank() {
            SyncFulfillmentStockCommand command =
                SyncFulfillmentStockCommand.of("CUST001", Arrays.asList(null, "  ", ""));
            assertThatThrownBy(() -> service.sync(command))
                .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
        }
    }

    @Nested
    @DisplayName("sync — fasstoProductCode 직접 송신")
    class SyncDirectFasstoProductCodeMapping {

        @Test
        @DisplayName("auto-gen 상품(Base62 22자) — Fastto에 fasstoProductCode가 cstGodCd로 전달되고 productKey 이벤트가 발행된다")
        void shouldSendFasstoProductCodeForAutoGen() {
            // given
            UUID productKey = UUID.randomUUID();
            when(findFulfillmentGoodsRegistrationPort.findProductKeyByFasstoProductCode(AUTO_GEN_FASSTO_CODE))
                .thenReturn(Optional.of(productKey));
            when(getFulfillmentStockDetailUseCase.getStockDetail(any(GetFulfillmentStockDetailCommand.class)))
                .thenReturn(GetFulfillmentStocksResult.of(1, List.of(buildStockInfo(AUTO_GEN_FASSTO_CODE, 5))));
            SyncFulfillmentStockCommand command =
                SyncFulfillmentStockCommand.of("CUST001", List.of(AUTO_GEN_FASSTO_CODE));

            // when
            service.sync(command);

            // then
            ArgumentCaptor<GetFulfillmentStockDetailCommand> captor =
                ArgumentCaptor.forClass(GetFulfillmentStockDetailCommand.class);
            verify(getFulfillmentStockDetailUseCase).getStockDetail(captor.capture());
            assertThat(captor.getValue().fasstoProductCode()).isEqualTo(AUTO_GEN_FASSTO_CODE);
            assertThat(captor.getValue().productKey()).isEqualTo(productKey);

            ArgumentCaptor<FulfillmentInventorySyncedEvent> eventCaptor =
                ArgumentCaptor.forClass(FulfillmentInventorySyncedEvent.class);
            verify(publishFulfillmentInventorySyncedEventPort).publish(eventCaptor.capture());
            FulfillmentInventorySyncedEvent.InventoryItem item = eventCaptor.getValue().inventories().get(0);
            assertThat(item.productKey()).isEqualTo(productKey);
            assertThat(item.stock()).isEqualTo(5);
        }

        @Test
        @DisplayName("import 상품(non-UUID, 예: VZL00001) — Fastto에 fasstoProductCode가 cstGodCd로 전달되고 productKey 이벤트가 발행된다 (#5187 회귀 방지)")
        void shouldSendFasstoProductCodeForImport() {
            // given
            UUID productKey = UUID.randomUUID();
            when(findFulfillmentGoodsRegistrationPort.findProductKeyByFasstoProductCode(IMPORT_FASSTO_CODE))
                .thenReturn(Optional.of(productKey));
            when(getFulfillmentStockDetailUseCase.getStockDetail(any(GetFulfillmentStockDetailCommand.class)))
                .thenReturn(GetFulfillmentStocksResult.of(1, List.of(buildStockInfo(IMPORT_FASSTO_CODE, 7))));
            SyncFulfillmentStockCommand command =
                SyncFulfillmentStockCommand.of("CUST001", List.of(IMPORT_FASSTO_CODE));

            // when
            service.sync(command);

            // then
            ArgumentCaptor<GetFulfillmentStockDetailCommand> captor =
                ArgumentCaptor.forClass(GetFulfillmentStockDetailCommand.class);
            verify(getFulfillmentStockDetailUseCase).getStockDetail(captor.capture());
            assertThat(captor.getValue().fasstoProductCode()).isEqualTo(IMPORT_FASSTO_CODE);
            assertThat(captor.getValue().productKey()).isEqualTo(productKey);

            ArgumentCaptor<FulfillmentInventorySyncedEvent> eventCaptor =
                ArgumentCaptor.forClass(FulfillmentInventorySyncedEvent.class);
            verify(publishFulfillmentInventorySyncedEventPort).publish(eventCaptor.capture());
            FulfillmentInventorySyncedEvent.InventoryItem item = eventCaptor.getValue().inventories().get(0);
            assertThat(item.productKey()).isEqualTo(productKey);
            assertThat(item.stock()).isEqualTo(7);
        }

        @Test
        @DisplayName("매칭되는 productKey가 없으면 해당 row를 skip하고 이벤트를 발행하지 않는다")
        void shouldSkipWhenProductKeyNotResolved() {
            // given
            when(findFulfillmentGoodsRegistrationPort.findProductKeyByFasstoProductCode("UNKNOWN"))
                .thenReturn(Optional.empty());
            SyncFulfillmentStockCommand command =
                SyncFulfillmentStockCommand.of("CUST001", List.of("UNKNOWN"));

            // when
            service.sync(command);

            // then
            verifyNoInteractions(getFulfillmentStockDetailUseCase);
            verify(publishFulfillmentInventorySyncedEventPort, never()).publish(any());
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

    @Nested
    @DisplayName("syncAll — fasstoProductCode 매칭 (#5187 회귀 방지)")
    class SyncAllFasstoProductCodeMatching {

        @Test
        @DisplayName("auto-gen 상품 customerProductCode(Base62 22자) — productKey 매칭 후 이벤트 발행")
        void shouldMatchAndPublishForAutoGenCode() {
            // given
            UUID productKey = UUID.randomUUID();
            when(findFulfillmentGoodsRegistrationPort.findProductKeyByFasstoProductCode(AUTO_GEN_FASSTO_CODE))
                .thenReturn(Optional.of(productKey));
            when(getFulfillmentStocksUseCase.getStocks(any(GetFulfillmentStocksCommand.class)))
                .thenReturn(GetFulfillmentStocksResult.of(1, List.of(buildStockInfo(AUTO_GEN_FASSTO_CODE, 3))));
            SyncFulfillmentAllStockCommand command = SyncFulfillmentAllStockCommand.of("CUST001");

            // when
            service.syncAll(command);

            // then
            ArgumentCaptor<FulfillmentInventorySyncedEvent> captor =
                ArgumentCaptor.forClass(FulfillmentInventorySyncedEvent.class);
            verify(publishFulfillmentInventorySyncedEventPort).publish(captor.capture());
            FulfillmentInventorySyncedEvent.InventoryItem item = captor.getValue().inventories().get(0);
            assertThat(item.productKey()).isEqualTo(productKey);
            assertThat(item.stock()).isEqualTo(3);
        }

        @Test
        @DisplayName("import 상품 customerProductCode(non-UUID, VZL00001) — productKey 매칭 후 이벤트 발행")
        void shouldMatchAndPublishForImportNonUuidCode() {
            // given
            UUID productKey = UUID.randomUUID();
            when(findFulfillmentGoodsRegistrationPort.findProductKeyByFasstoProductCode(IMPORT_FASSTO_CODE))
                .thenReturn(Optional.of(productKey));
            when(getFulfillmentStocksUseCase.getStocks(any(GetFulfillmentStocksCommand.class)))
                .thenReturn(GetFulfillmentStocksResult.of(1, List.of(buildStockInfo(IMPORT_FASSTO_CODE, 11))));
            SyncFulfillmentAllStockCommand command = SyncFulfillmentAllStockCommand.of("CUST001");

            // when
            service.syncAll(command);

            // then
            ArgumentCaptor<FulfillmentInventorySyncedEvent> captor =
                ArgumentCaptor.forClass(FulfillmentInventorySyncedEvent.class);
            verify(publishFulfillmentInventorySyncedEventPort).publish(captor.capture());
            FulfillmentInventorySyncedEvent.InventoryItem item = captor.getValue().inventories().get(0);
            assertThat(item.productKey()).isEqualTo(productKey);
            assertThat(item.stock()).isEqualTo(11);
        }

        @Test
        @DisplayName("같은 customerProductCode의 lot row가 여러 개이면 availableStockQuantity가 합산된다")
        void shouldAccumulateMultipleLotRowsByCode() {
            // given
            UUID productKey = UUID.randomUUID();
            when(findFulfillmentGoodsRegistrationPort.findProductKeyByFasstoProductCode(IMPORT_FASSTO_CODE))
                .thenReturn(Optional.of(productKey));
            when(getFulfillmentStocksUseCase.getStocks(any(GetFulfillmentStocksCommand.class)))
                .thenReturn(GetFulfillmentStocksResult.of(2, List.of(
                    buildStockInfo(IMPORT_FASSTO_CODE, 3),
                    buildStockInfo(IMPORT_FASSTO_CODE, 5)
                )));
            SyncFulfillmentAllStockCommand command = SyncFulfillmentAllStockCommand.of("CUST001");

            // when
            service.syncAll(command);

            // then
            ArgumentCaptor<FulfillmentInventorySyncedEvent> captor =
                ArgumentCaptor.forClass(FulfillmentInventorySyncedEvent.class);
            verify(publishFulfillmentInventorySyncedEventPort).publish(captor.capture());
            FulfillmentInventorySyncedEvent.InventoryItem item = captor.getValue().inventories().get(0);
            assertThat(item.productKey()).isEqualTo(productKey);
            assertThat(item.stock()).isEqualTo(8);
        }

        @Test
        @DisplayName("매칭되는 productKey가 없는 row는 skip된다 — 이벤트 미발행")
        void shouldSkipUnmatchedRows() {
            // given
            when(findFulfillmentGoodsRegistrationPort.findProductKeyByFasstoProductCode("UNKNOWN"))
                .thenReturn(Optional.empty());
            when(getFulfillmentStocksUseCase.getStocks(any(GetFulfillmentStocksCommand.class)))
                .thenReturn(GetFulfillmentStocksResult.of(1, List.of(buildStockInfo("UNKNOWN", 9))));
            SyncFulfillmentAllStockCommand command = SyncFulfillmentAllStockCommand.of("CUST001");

            // when
            service.syncAll(command);

            // then
            verify(publishFulfillmentInventorySyncedEventPort, never()).publish(any());
        }

        @Test
        @DisplayName("Fastto stocks 응답이 0건이면 이벤트를 발행하지 않는다 (graceful)")
        void shouldNotPublishWhenStocksEmpty() {
            // given
            when(getFulfillmentStocksUseCase.getStocks(any(GetFulfillmentStocksCommand.class)))
                .thenReturn(GetFulfillmentStocksResult.of(0, List.of()));
            SyncFulfillmentAllStockCommand command = SyncFulfillmentAllStockCommand.of("CUST001");

            // when
            service.syncAll(command);

            // then
            verify(publishFulfillmentInventorySyncedEventPort, never()).publish(any());
        }
    }
}
