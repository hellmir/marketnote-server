package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentShopCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentShopResult;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentShopPort;
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
@DisplayName("풀필먼트 출고처 등록 테스트")
class RegisterFulfillmentShopUseCaseTest {
    @InjectMocks
    private RegisterFulfillmentShopService service;
    @Mock
    private RegisterFulfillmentShopPort registerFulfillmentShopPort;

    @Test
    @DisplayName("Command를 Port에 위임하여 결과를 반환한다")
    void shouldDelegateToPort() {
        // given
        RegisterFulfillmentShopCommand command = new RegisterFulfillmentShopCommand(
                "CUST001", "token", "marketnote", "CST01", "20260401", "20270401",
                "12345", "addr1", "addr2", "ceo", "123-45-67890", "01012345678",
                "01", "01", "Y", "01", "emp", "manager", "01098765432", "Y"
        );
        RegisterFulfillmentShopResult expectedResult = RegisterFulfillmentShopResult.of("등록 성공", "200", "SHOP001");
        when(registerFulfillmentShopPort.registerShop(any())).thenReturn(expectedResult);
        // when
        RegisterFulfillmentShopResult result = service.registerShop(command);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(registerFulfillmentShopPort).registerShop(any());
    }
}
