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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetFulfillmentShopsService 테스트")
class GetFulfillmentShopsUseCaseTest {
    @InjectMocks
    private GetFulfillmentShopsService getFulfillmentShopsService;

    @Mock
    private GetFulfillmentShopsPort getFulfillmentShopsPort;

    @Test
    @DisplayName("출고처 목록 조회 커맨드를 전달하면 포트를 통해 출고처 목록을 반환한다")
    void shouldReturnShopsFromPort() {
        // given
        GetFulfillmentShopsCommand command = GetFulfillmentShopsCommand.of("CUST001", "token");
        GetFulfillmentShopsResult expectedResult = GetFulfillmentShopsResult.of(0, List.of());
        when(getFulfillmentShopsPort.getShops(any())).thenReturn(expectedResult);

        // when
        GetFulfillmentShopsResult result = getFulfillmentShopsService.getShops(command);

        // then
        assertThat(result).isEqualTo(expectedResult);
        verify(getFulfillmentShopsPort).getShops(any());
    }
}
