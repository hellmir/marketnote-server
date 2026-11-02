package com.personal.marketnote.fulfillment.service.vendor;

import com.personal.marketnote.fulfillment.port.in.command.vendor.GetFulfillmentShopsCommand;
import com.personal.marketnote.fulfillment.port.in.result.vendor.GetFulfillmentShopsResult;
import com.personal.marketnote.fulfillment.port.out.vendor.GetFulfillmentShopsPort;
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
@DisplayName("풀필먼트 출고처 목록 조회 테스트")
class GetFulfillmentShopsUseCaseTest {
    @InjectMocks
    private GetFulfillmentShopsService getFulfillmentShopsService;

    @Mock
    private GetFulfillmentShopsPort getFulfillmentShopsPort;

    @Test
    @DisplayName("출고처 목록 조회 커맨드를 포트에 직접 전달하여 결과를 반환한다")
    void shouldDelegateCommandDirectlyToPort() {
        // given
        GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "token");
        GetFulfillmentShopsResult expectedResult = GetFulfillmentShopsResult.of(0, List.of());
        when(getFulfillmentShopsPort.getShops(command)).thenReturn(expectedResult);

        // when
        GetFulfillmentShopsResult result = getFulfillmentShopsService.getShops(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentShopsPort).getShops(command);
    }
}
