package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.common.kafka.event.FulfillmentGoodsSyncedEvent;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentGoodsItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentGoodsItemResult;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentGoodsResult;
import com.personal.marketnote.fulfillment.port.out.event.PublishFulfillmentGoodsSyncedEventPort;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentGoodsPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateFulfillmentGoodsService 테스트")
class UpdateFulfillmentGoodsUseCaseTest {
    @InjectMocks
    private UpdateFulfillmentGoodsService updateFulfillmentGoodsService;

    @Mock
    private UpdateFulfillmentGoodsPort updateFulfillmentGoodsPort;

    @Mock
    private PublishFulfillmentGoodsSyncedEventPort publishFulfillmentGoodsSyncedEventPort;

    @Test
    @DisplayName("상품 수정 커맨드를 전달하면 포트를 통해 수정 결과를 반환한다")
    void shouldReturnUpdateResultFromPort() {
        // given
        UpdateFulfillmentGoodsItemCommand itemCommand = UpdateFulfillmentGoodsItemCommand.of(
                "PROD001", "테스트 상품", "1", "01",
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null
        );
        UpdateFulfillmentGoodsCommand command = UpdateFulfillmentGoodsCommand.of(
                "CUST001", "token", List.of(itemCommand)
        );
        UpdateFulfillmentGoodsResult expectedResult = UpdateFulfillmentGoodsResult.of(1, List.of(
                UpdateFulfillmentGoodsItemResult.of("SLIP001", "ORD001", "수정 성공", "200", null)
        ));
        when(updateFulfillmentGoodsPort.updateGoods(any())).thenReturn(expectedResult);

        // when
        UpdateFulfillmentGoodsResult result = updateFulfillmentGoodsService.updateGoods(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(updateFulfillmentGoodsPort).updateGoods(any());
    }

    @Test
    @DisplayName("상품 목록의 각 항목에 대해 동기화 이벤트를 발행한다")
    void shouldPublishSyncedEventForEachGoodsItem() {
        // given
        UpdateFulfillmentGoodsItemCommand item1 = UpdateFulfillmentGoodsItemCommand.of(
                "PROD001", "상품1", "1", "01",
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null
        );
        UpdateFulfillmentGoodsItemCommand item2 = UpdateFulfillmentGoodsItemCommand.of(
                "PROD002", "상품2", "1", "01",
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null, null, null, null, null, null,
                null
        );
        UpdateFulfillmentGoodsCommand command = UpdateFulfillmentGoodsCommand.of(
                "CUST001", "token", List.of(item1, item2)
        );
        UpdateFulfillmentGoodsResult expectedResult = UpdateFulfillmentGoodsResult.of(2, List.of());
        when(updateFulfillmentGoodsPort.updateGoods(any())).thenReturn(expectedResult);

        // when
        updateFulfillmentGoodsService.updateGoods(command);

        // then
        verify(publishFulfillmentGoodsSyncedEventPort, times(2)).publish(any(FulfillmentGoodsSyncedEvent.class));
    }

    @Test
    @DisplayName("빈 상품 목록이면 이벤트를 발행하지 않는다")
    void shouldNotPublishEventWhenGoodsListIsEmpty() {
        // given
        UpdateFulfillmentGoodsCommand command = UpdateFulfillmentGoodsCommand.of(
                "CUST001", "token", List.of()
        );
        UpdateFulfillmentGoodsResult expectedResult = UpdateFulfillmentGoodsResult.of(0, List.of());
        when(updateFulfillmentGoodsPort.updateGoods(any())).thenReturn(expectedResult);

        // when
        updateFulfillmentGoodsService.updateGoods(command);

        // then
        verifyNoInteractions(publishFulfillmentGoodsSyncedEventPort);
    }
}
