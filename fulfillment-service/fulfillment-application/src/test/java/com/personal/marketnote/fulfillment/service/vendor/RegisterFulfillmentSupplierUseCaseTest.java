package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.RegisterFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.RegisterFulfillmentSupplierResult;
import com.personal.marketnote.fulfillment.port.out.vendor.RegisterFulfillmentSupplierPort;
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
@DisplayName("풀필먼트 공급처 등록 테스트")
class RegisterFulfillmentSupplierUseCaseTest {
    @InjectMocks
    private RegisterFulfillmentSupplierService service;
    @Mock
    private RegisterFulfillmentSupplierPort registerFulfillmentSupplierPort;

    @Test
    @DisplayName("Command를 Port에 위임하여 결과를 반환한다")
    void shouldDelegateToPort() {
        // given
        RegisterFulfillmentSupplierCommand command = new RegisterFulfillmentSupplierCommand(
                "CUST001", "token", "supplier", "CST01", "Y", "20260401", "20270401",
                "12345", "addr1", "addr2", "ceo", "123-45-67890", "busSp", "busTp",
                "01012345678", "0212345678", "emp1", "manager1", "01011111111", "emp1@test.com",
                "emp2", "manager2", "01022222222", "emp2@test.com"
        );
        RegisterFulfillmentSupplierResult expectedResult = RegisterFulfillmentSupplierResult.of("등록 성공", "200", "SUP001");
        when(registerFulfillmentSupplierPort.registerSupplier(any())).thenReturn(expectedResult);
        // when
        RegisterFulfillmentSupplierResult result = service.registerSupplier(command);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(registerFulfillmentSupplierPort).registerSupplier(any());
    }
}
