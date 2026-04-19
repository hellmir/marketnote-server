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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("풀필먼트 공급사 수정 테스트")
class UpdateFulfillmentSupplierUseCaseTest {
    @InjectMocks
    private UpdateFulfillmentSupplierService service;

    @Mock
    private UpdateFulfillmentSupplierPort updateFulfillmentSupplierPort;

    @Test
    @DisplayName("공급사 수정 커맨드를 포트에 직접 전달하여 결과를 반환한다")
    void shouldDelegateCommandDirectlyToPort() {
        // given
        UpdateFulfillmentSupplierCommand command = UpdateFulfillmentSupplierCommand.of(
                "CUST001", "token", "SUP001", "공급사명", "CST01", "Y", "20260401", "20270401",
                "12345", "서울시", "강남구", "대표자", "123-45-67890", "도매", "식품",
                "01012345678", "0212345678", "담당자1", "매니저", "01011111111", "emp1@test.com",
                "담당자2", "부매니저", "01022222222", "emp2@test.com"
        );
        UpdateFulfillmentSupplierResult expectedResult = UpdateFulfillmentSupplierResult.of("수정 성공", "200", "SUP001");
        when(updateFulfillmentSupplierPort.updateSupplier(command)).thenReturn(expectedResult);

        // when
        UpdateFulfillmentSupplierResult result = service.updateSupplier(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(updateFulfillmentSupplierPort).updateSupplier(command);
    }
}
