package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsElementsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsElementsResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentGoodsElementsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("풀필먼트 상품 구성 요소 조회 테스트")
class GetFulfillmentGoodsElementsUseCaseTest {
    @InjectMocks
    private GetFulfillmentGoodsElementsService service;
    @Mock
    private GetFulfillmentGoodsElementsPort getFulfillmentGoodsElementsPort;

    @Test
    @DisplayName("Command를 Port에 위임하여 결과를 반환한다")
    void shouldDelegateToPort() {
        // given
        GetFulfillmentGoodsElementsCommand command = GetFulfillmentGoodsElementsCommand.of("CUST001", "token");
        GetFulfillmentGoodsElementsResult expectedResult = GetFulfillmentGoodsElementsResult.of(0, List.of());
        when(getFulfillmentGoodsElementsPort.getGoodsElements(any())).thenReturn(expectedResult);
        // when
        GetFulfillmentGoodsElementsResult result = service.getGoodsElements(command);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentGoodsElementsPort).getGoodsElements(any());
    }
}
