package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentSuppliersCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentSuppliersResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentSuppliersPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("풀필먼트 공급사 목록 조회 테스트")
class GetFulfillmentSuppliersUseCaseTest {
    @InjectMocks
    private GetFulfillmentSuppliersService service;

    @Mock
    private GetFulfillmentSuppliersPort getFulfillmentSuppliersPort;

    @Test
    @DisplayName("공급사 목록 조회 커맨드를 포트에 직접 전달하여 결과를 반환한다")
    void shouldDelegateCommandDirectlyToPort() {
        // given
        GetFulfillmentSuppliersCommand command = GetFulfillmentSuppliersCommand.of("CUST001", "token");
        GetFulfillmentSuppliersResult expectedResult = GetFulfillmentSuppliersResult.of(0, List.of());
        when(getFulfillmentSuppliersPort.getSuppliers(command)).thenReturn(expectedResult);

        // when
        GetFulfillmentSuppliersResult result = service.getSuppliers(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentSuppliersPort).getSuppliers(command);
    }
}
