package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentGoodsPort;
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
@DisplayName("GetFulfillmentGoodsService 테스트")
class GetFulfillmentGoodsUseCaseTest {
    @InjectMocks
    private GetFulfillmentGoodsService getFulfillmentGoodsService;

    @Mock
    private GetFulfillmentGoodsPort getFulfillmentGoodsPort;

    @Test
    @DisplayName("상품 목록 조회 커맨드를 전달하면 포트를 통해 상품 목록을 반환한다")
    void shouldReturnGoodsFromPort() {
        // given
        GetFulfillmentGoodsCommand command = GetFulfillmentGoodsCommand.of("CUST001", "token");
        GetFulfillmentGoodsResult expectedResult = GetFulfillmentGoodsResult.of(0, List.of());
        when(getFulfillmentGoodsPort.getGoods(any())).thenReturn(expectedResult);

        // when
        GetFulfillmentGoodsResult result = getFulfillmentGoodsService.getGoods(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentGoodsPort).getGoods(any());
    }
}
