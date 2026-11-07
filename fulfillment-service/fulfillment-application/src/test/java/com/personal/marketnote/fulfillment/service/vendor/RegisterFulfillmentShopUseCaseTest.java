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
    @DisplayName("출고처 등록 커맨드를 포트에 직접 전달하여 결과를 반환한다")
    void shouldDelegateCommandDirectlyToPort() {
        // given
        RegisterFulfillmentShopCommand command = RegisterFulfillmentShopCommand.builder()
                .customerCode("CUST001")
                .accessToken("token")
                .shopName("출고처명")
                .customerShopCode("CST01")
                .dealStartDate("20260401")
                .dealEndDate("20270401")
                .zipCode("12345")
                .address1("서울시")
                .address2("강남구")
                .ceoName("대표자")
                .businessNumber("123-45-67890")
                .phoneNumber("01012345678")
                .unloadMethod("01")
                .inspectionMethod("01")
                .standbyYn("Y")
                .formType("01")
                .managerName("담당자")
                .managerPosition("매니저")
                .managerPhoneNumber("01098765432")
                .useYn("Y")
                .build();
        RegisterFulfillmentShopResult expectedResult = RegisterFulfillmentShopResult.of("등록 성공", "200", "SHOP001");
        when(registerFulfillmentShopPort.registerShop(command)).thenReturn(expectedResult);

        // when
        RegisterFulfillmentShopResult result = service.registerShop(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(registerFulfillmentShopPort).registerShop(command);
    }
}
