package com.personal.marketnote.fulfillment.service;

import com.personal.marketnote.fulfillment.domain.FulfillmentAccessToken;
import com.personal.marketnote.fulfillment.exception.FulfillmentInvalidAccessException;
import com.personal.marketnote.fulfillment.port.in.command.GetInternalReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentReturnGodDetailCommand;
import com.personal.marketnote.fulfillment.port.in.result.GetInternalReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentReturnGodDetailGoodsResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.FulfillmentReturnGodDetailInfoResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentReturnGodDetailResult;
import com.personal.marketnote.fulfillment.port.in.usecase.vendor.RequestFulfillmentAuthUseCase;
import com.personal.marketnote.fulfillment.port.out.vendor.DisconnectFulfillmentAuthPort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentCustomerCodePort;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentReturnGodDetailPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetInternalReturnGodDetailService")
class GetInternalReturnGodDetailUseCaseTest {

    @Mock
    private GetFulfillmentCustomerCodePort getFulfillmentCustomerCodePort;

    @Mock
    private RequestFulfillmentAuthUseCase requestFulfillmentAuthUseCase;

    @Mock
    private DisconnectFulfillmentAuthPort disconnectFulfillmentAuthPort;

    @Mock
    private GetFulfillmentReturnGodDetailPort getFulfillmentReturnGodDetailPort;

    @InjectMocks
    private GetInternalReturnGodDetailService service;

    private static final String CUSTOMER_CODE = "CUSTOMER-01";
    private static final String ACCESS_TOKEN_VALUE = "token-1";
    private static final String REISSUED_TOKEN_VALUE = "token-2";

    private GetInternalReturnGodDetailCommand command() {
        return GetInternalReturnGodDetailCommand.of("SLIP-001,SLIP-002");
    }

    private FulfillmentAccessToken firstToken() {
        return FulfillmentAccessToken.of(ACCESS_TOKEN_VALUE, "20991231235959");
    }

    private FulfillmentAccessToken reissuedToken() {
        return FulfillmentAccessToken.of(REISSUED_TOKEN_VALUE, "20991231235959");
    }

    @Nested
    @DisplayName("성공 경로")
    class SuccessPath {

        @Test
        @DisplayName("정상적으로 반품 상세를 조회하고 최종적으로 accessToken을 disconnect한다")
        void returnsResultAndDisconnects() {
            GetInternalReturnGodDetailCommand cmd = command();
            when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(firstToken());
            when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn(CUSTOMER_CODE);
            FulfillmentReturnGodDetailGoodsResult goods = FulfillmentReturnGodDetailGoodsResult.of(
                    "PROD-1", "상품1", "2025-01-01", "2030-01-01", "2", "비고", "ST", "상태명"
            );
            FulfillmentReturnGodDetailInfoResult info = FulfillmentReturnGodDetailInfoResult.of(
                    "ORDER-1", "SUP", "IN-1", CUSTOMER_CODE, "WH", "IN-ORD-1",
                    "20250101", "OUT-1", "공급사", "수령자", "INV-1",
                    "RTN-1", "C1", "착불", "0", "N", "RT1", "회수",
                    "010-0000-0000", "메모", "RR", "사유명", "상세사유",
                    "CL1", "분류명", List.of(goods)
            );
            when(getFulfillmentReturnGodDetailPort.getReturnGodDetail(any(GetFulfillmentReturnGodDetailCommand.class)))
                    .thenReturn(GetFulfillmentReturnGodDetailResult.of(1, List.of(info)));

            GetInternalReturnGodDetailResult result = service.getReturnGodDetail(cmd);

            assertThat(result.dataCount()).isEqualTo(1);
            assertThat(result.returnGodInfos()).hasSize(1);
            assertThat(result.returnGodInfos().get(0).products()).hasSize(1);
            verify(disconnectFulfillmentAuthPort).disconnectAccessToken(ACCESS_TOKEN_VALUE);
        }

        @Test
        @DisplayName("반품 상세 응답이 비어 있으면 dataCount=0, 빈 리스트를 반환한다")
        void returnsEmptyResultWhenFasstoResultEmpty() {
            GetInternalReturnGodDetailCommand cmd = command();
            when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(firstToken());
            when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn(CUSTOMER_CODE);
            when(getFulfillmentReturnGodDetailPort.getReturnGodDetail(any(GetFulfillmentReturnGodDetailCommand.class)))
                    .thenReturn(GetFulfillmentReturnGodDetailResult.of(0, List.of()));

            GetInternalReturnGodDetailResult result = service.getReturnGodDetail(cmd);

            assertThat(result.dataCount()).isEqualTo(0);
            assertThat(result.returnGodInfos()).isEmpty();
            verify(disconnectFulfillmentAuthPort).disconnectAccessToken(ACCESS_TOKEN_VALUE);
        }

        @Test
        @DisplayName("반품 상세 info의 products가 null이면 빈 goods 리스트로 매핑된다")
        void mapsInfoWithoutProductsAsEmpty() {
            GetInternalReturnGodDetailCommand cmd = command();
            when(requestFulfillmentAuthUseCase.requestAccessToken()).thenReturn(firstToken());
            when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn(CUSTOMER_CODE);
            FulfillmentReturnGodDetailInfoResult info = FulfillmentReturnGodDetailInfoResult.of(
                    "ORDER-1", null, null, CUSTOMER_CODE, null, "IN-ORD-1",
                    null, null, null, null, null,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null,
                    null, null, null
            );
            when(getFulfillmentReturnGodDetailPort.getReturnGodDetail(any(GetFulfillmentReturnGodDetailCommand.class)))
                    .thenReturn(GetFulfillmentReturnGodDetailResult.of(1, List.of(info)));

            GetInternalReturnGodDetailResult result = service.getReturnGodDetail(cmd);

            assertThat(result.returnGodInfos()).hasSize(1);
            assertThat(result.returnGodInfos().get(0).products()).isEmpty();
        }
    }

    @Nested
    @DisplayName("INVALID_ACCESS 재시도 경로")
    class InvalidAccessRetry {

        @Test
        @DisplayName("첫 호출에서 INVALID_ACCESS 발생 시 캐시를 무효화하고 재요청 후 disconnect한다")
        void invalidatesCacheAndRetries() {
            GetInternalReturnGodDetailCommand cmd = command();
            when(requestFulfillmentAuthUseCase.requestAccessToken())
                    .thenReturn(firstToken())
                    .thenReturn(reissuedToken());
            when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn(CUSTOMER_CODE);
            when(getFulfillmentReturnGodDetailPort.getReturnGodDetail(any(GetFulfillmentReturnGodDetailCommand.class)))
                    .thenThrow(new FulfillmentInvalidAccessException())
                    .thenReturn(GetFulfillmentReturnGodDetailResult.of(0, List.of()));

            GetInternalReturnGodDetailResult result = service.getReturnGodDetail(cmd);

            assertThat(result).isNotNull();
            verify(requestFulfillmentAuthUseCase).invalidateCache();
            verify(requestFulfillmentAuthUseCase, times(2)).requestAccessToken();
            verify(getFulfillmentReturnGodDetailPort, times(2))
                    .getReturnGodDetail(any(GetFulfillmentReturnGodDetailCommand.class));
            verify(disconnectFulfillmentAuthPort).disconnectAccessToken(REISSUED_TOKEN_VALUE);
        }

        @Test
        @DisplayName("재시도에서도 예외가 발생하면 예외가 전파되고 최종적으로 disconnect가 호출된다")
        void propagatesExceptionAndStillDisconnects() {
            GetInternalReturnGodDetailCommand cmd = command();
            when(requestFulfillmentAuthUseCase.requestAccessToken())
                    .thenReturn(firstToken())
                    .thenReturn(reissuedToken());
            when(getFulfillmentCustomerCodePort.getCustomerCode()).thenReturn(CUSTOMER_CODE);
            FulfillmentInvalidAccessException finalFailure = new FulfillmentInvalidAccessException();
            when(getFulfillmentReturnGodDetailPort.getReturnGodDetail(any(GetFulfillmentReturnGodDetailCommand.class)))
                    .thenThrow(new FulfillmentInvalidAccessException())
                    .thenThrow(finalFailure);

            assertThatThrownBy(() -> service.getReturnGodDetail(cmd))
                    .isInstanceOf(FulfillmentInvalidAccessException.class);

            verify(disconnectFulfillmentAuthPort).disconnectAccessToken(REISSUED_TOKEN_VALUE);
        }
    }
}
