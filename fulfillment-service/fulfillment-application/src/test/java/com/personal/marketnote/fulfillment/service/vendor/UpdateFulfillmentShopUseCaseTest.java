package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentShopResult;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentShopPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("풀필먼트 출고처 수정 테스트")
class UpdateFulfillmentShopUseCaseTest {
    @InjectMocks
    private UpdateFulfillmentShopService service;
    @Mock
    private UpdateFulfillmentShopPort updateFulfillmentShopPort;

    @Test
    @DisplayName("Command를 Port에 위임하여 결과를 반환한다")
    void shouldDelegateToPort() {
        // given
        UpdateFulfillmentShopCommand command = new UpdateFulfillmentShopCommand(
                "CUST001", "token", "SHOP001", "marketnote", "CST01", "20260401", "20270401",
                "12345", "addr1", "addr2", "ceo", "123-45-67890", "01012345678",
                "01", "01", "Y", "01", "emp", "manager", "01098765432", "Y"
        );
        UpdateFulfillmentShopResult expectedResult = UpdateFulfillmentShopResult.of("수정 성공", "200", "SHOP001");
        when(updateFulfillmentShopPort.updateShop(any())).thenReturn(expectedResult);
        // when
        UpdateFulfillmentShopResult result = service.updateShop(command);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(updateFulfillmentShopPort).updateShop(any());
    }
}
