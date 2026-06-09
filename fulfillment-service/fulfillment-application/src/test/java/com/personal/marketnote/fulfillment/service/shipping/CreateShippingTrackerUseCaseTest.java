package com.personal.marketnote.fulfillment.service.shipping;

import com.personal.marketnote.fulfillment.domain.exception.FulfillmentQueryParameterNoValueException;
import com.personal.marketnote.fulfillment.domain.shipping.ShippingTracker;
import com.personal.marketnote.fulfillment.port.in.command.CreateShippingTrackerCommand;
import com.personal.marketnote.fulfillment.port.out.shipping.SaveShippingTrackerPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateShippingTrackerUseCase 테스트")
class CreateShippingTrackerUseCaseTest {

    @InjectMocks
    private CreateShippingTrackerService createShippingTrackerService;

    @Mock
    private SaveShippingTrackerPort saveShippingTrackerPort;

    @Test
    @DisplayName("orderId로 ShippingTracker를 생성하면 PREPARING 상태로 저장된다")
    void createShippingTrackerWithOrderId() {
        // given
        CreateShippingTrackerCommand command = new CreateShippingTrackerCommand(100L);

        // when
        createShippingTrackerService.createShippingTracker(command);

        // then
        ArgumentCaptor<ShippingTracker> captor = ArgumentCaptor.forClass(ShippingTracker.class);
        verify(saveShippingTrackerPort).save(captor.capture());

        ShippingTracker savedTracker = captor.getValue();
        assertThat(savedTracker.getOrderId()).isEqualTo(100L);
        assertThat(savedTracker.isPreparing()).isTrue();
        assertThat(savedTracker.isPollingActive()).isTrue();
    }

    @Test
    @DisplayName("orderId가 null이면 예외가 발생한다")
    void createShippingTrackerWithNullOrderId() {
        // given
        CreateShippingTrackerCommand command = new CreateShippingTrackerCommand(null);

        // when & then
        assertThatThrownBy(() -> createShippingTrackerService.createShippingTracker(command))
                .isInstanceOf(FulfillmentQueryParameterNoValueException.class);
    }
}
