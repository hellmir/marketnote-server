package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.UpdateFulfillmentSupplierCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.UpdateFulfillmentSupplierResult;
import com.personal.marketnote.fulfillment.port.out.vendor.UpdateFulfillmentSupplierPort;
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
@DisplayName("풀필먼트 공급처 수정 테스트")
class UpdateFulfillmentSupplierUseCaseTest {
    @InjectMocks
    private UpdateFulfillmentSupplierService service;
    @Mock
    private UpdateFulfillmentSupplierPort updateFulfillmentSupplierPort;

    @Test
    @DisplayName("Command를 Port에 위임하여 결과를 반환한다")
    void shouldDelegateToPort() {
        // given
        UpdateFulfillmentSupplierCommand command = new UpdateFulfillmentSupplierCommand(
                "CUST001", "token", "SUP001", "supplier", "CST01", "Y", "20260401", "20270401",
                "12345", "addr1", "addr2", "ceo", "123-45-67890", "busSp", "busTp",
                "01012345678", "0212345678", "emp1", "manager1", "01011111111", "emp1@test.com",
                "emp2", "manager2", "01022222222", "emp2@test.com"
        );
        UpdateFulfillmentSupplierResult expectedResult = UpdateFulfillmentSupplierResult.of("수정 성공", "200", "SUP001");
        when(updateFulfillmentSupplierPort.updateSupplier(any())).thenReturn(expectedResult);
        // when
        UpdateFulfillmentSupplierResult result = service.updateSupplier(command);
        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(updateFulfillmentSupplierPort).updateSupplier(any());
    }
}
