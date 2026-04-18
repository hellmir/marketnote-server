package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingGoodsCommand;
import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentWarehousingItemCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentWarehousingResult;
import com.personal.marketnote.fulfillment.port.out.scheduler.ScheduleFulfillmentWarehousingPollingPort;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentWarehousingPort;
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
@DisplayName("풀필먼트 입고 등록 테스트")
class RegisterFulfillmentWarehousingUseCaseTest {
    @InjectMocks
    private RegisterFulfillmentWarehousingService service;
    @Mock
    private RegisterFulfillmentWarehousingPort registerFulfillmentWarehousingPort;
    @Mock
    private ScheduleFulfillmentWarehousingPollingPort scheduleFulfillmentWarehousingPollingPort;

    @Test
    @DisplayName("Command를 Port에 위임하여 결과를 반환한다")
    void shouldDelegateToPort() {
        // given
        RegisterFulfillmentWarehousingItemCommand item = RegisterFulfillmentWarehousingItemCommand.builder()
                .orderNumber("ORD001")
                .orderDate("20260402")
                .warehousingMethod("01")
                .products(List.of(RegisterFulfillmentWarehousingGoodsCommand.of("GOD001", "20260501", 10)))
                .build();
        RegisterFulfillmentWarehousingCommand command = RegisterFulfillmentWarehousingCommand.of("CUST001", "token", List.of(item));
        RegisterFulfillmentWarehousingResult expectedResult = RegisterFulfillmentWarehousingResult.of(0, List.of());
        when(registerFulfillmentWarehousingPort.registerWarehousing(any())).thenReturn(expectedResult);
        // when
        RegisterFulfillmentWarehousingResult result = service.registerWarehousing(command);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(registerFulfillmentWarehousingPort).registerWarehousing(any());
    }
}
