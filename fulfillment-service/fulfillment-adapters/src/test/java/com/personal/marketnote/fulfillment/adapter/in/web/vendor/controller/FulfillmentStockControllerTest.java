package com.personal.marketnote.fulfillment.adapter.in.web.vendor.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.fulfillment.adapter.in.web.vendor.response.GetFulfillmentStocksResponse;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStockDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentStocksCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.SyncFulfillmentAllStockCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentStocksResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentStockDetailUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.GetFulfillmentStocksUseCase;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.SyncFulfillmentAllStockUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentStockController 풀필먼트 재고 관리")
class FulfillmentStockControllerTest {

    private static final String CUSTOMER_CODE = "CUST001";
    private static final String ACCESS_TOKEN = "TOKEN";

    @InjectMocks
    private FulfillmentStockController controller;

    @Mock
    private GetFulfillmentStocksUseCase getFulfillmentStocksUseCase;
    @Mock
    private GetFulfillmentStockDetailUseCase getFulfillmentStockDetailUseCase;
    @Mock
    private SyncFulfillmentAllStockUseCase syncFulfillmentAllStockUseCase;

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/stocks/{customerCode} - 재고 목록 조회")
    class GetStocks {

        @Test
        @DisplayName("쿼리 파라미터를 Command로 매핑하여 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentStocksUseCase.getStocks(any(GetFulfillmentStocksCommand.class)))
                    .thenReturn(GetFulfillmentStocksResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentStocksResponse>> response =
                    controller.getStocks(CUSTOMER_CODE, ACCESS_TOKEN, "Y", "WH01");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentStocksUseCase).getStocks(any(GetFulfillmentStocksCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vendors/fassto/stocks/detail/{customerCode} - 단일 상품 재고 조회")
    class GetStockDetail {

        @Test
        @DisplayName("상품코드를 Command로 매핑하여 상세 UseCase에 위임하고 OK를 반환한다")
        void returnsOk() {
            // given
            when(getFulfillmentStockDetailUseCase.getStockDetail(any(GetFulfillmentStockDetailCommand.class)))
                    .thenReturn(GetFulfillmentStocksResult.of(0, List.of()));

            // when
            ResponseEntity<BaseResponse<GetFulfillmentStocksResponse>> response =
                    controller.getStockDetail(CUSTOMER_CODE, ACCESS_TOKEN, "GOD-1", "N");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getFulfillmentStockDetailUseCase).getStockDetail(any(GetFulfillmentStockDetailCommand.class));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/vendors/fassto/stocks/sync/all/{customerCode} - 전체 재고 동기화")
    class SyncAllStocks {

        @Test
        @DisplayName("정상 요청 시 OK를 반환하고 전체 동기화 UseCase에 위임한다")
        void returnsOk() {
            // when
            ResponseEntity<BaseResponse<Void>> response = controller.syncAllStocks(CUSTOMER_CODE, "WH01");

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(syncFulfillmentAllStockUseCase).syncAll(any(SyncFulfillmentAllStockCommand.class));
        }
    }
}
