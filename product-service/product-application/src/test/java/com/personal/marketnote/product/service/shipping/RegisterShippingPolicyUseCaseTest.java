package com.personal.marketnote.product.service.shipping;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.common.kafka.event.ShippingPolicyChangeAction;
import com.personal.marketnote.product.domain.shipping.InvalidFreeShippingThresholdException;
import com.personal.marketnote.product.domain.shipping.InvalidShippingFeeException;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.domain.shipping.ShippingPolicySnapshotState;
import com.personal.marketnote.product.exception.ShippingPolicyAlreadyExistsException;
import com.personal.marketnote.product.port.in.command.RegisterShippingPolicyCommand;
import com.personal.marketnote.product.port.in.result.shipping.RegisterShippingPolicyResult;
import com.personal.marketnote.product.port.out.event.PublishShippingPolicyEventPort;
import com.personal.marketnote.product.port.out.shipping.FindShippingPolicyPort;
import com.personal.marketnote.product.port.out.shipping.SaveShippingPolicyPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterShippingPolicyUseCase 테스트")
class RegisterShippingPolicyUseCaseTest {

    @InjectMocks
    private RegisterShippingPolicyService registerShippingPolicyService;

    @Mock
    private FindShippingPolicyPort findShippingPolicyPort;

    @Mock
    private SaveShippingPolicyPort saveShippingPolicyPort;

    @Mock
    private PublishShippingPolicyEventPort publishShippingPolicyEventPort;

    private ShippingPolicy createSavedPolicy(Long sellerId) {
        return ShippingPolicy.from(ShippingPolicySnapshotState.builder()
                .id(1L)
                .sellerId(sellerId)
                .deliveryCompany("한진택배")
                .shippingFee(3000L)
                .freeShippingThreshold(20000L)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.of(2026, 3, 10, 10, 0))
                .modifiedAt(LocalDateTime.of(2026, 3, 10, 10, 0))
                .build());
    }

    @Test
    @DisplayName("배송비 정책을 정상 등록한다")
    void shouldRegisterShippingPolicySuccessfully() {
        // given
        Long sellerId = 10L;
        when(findShippingPolicyPort.findActiveBySellerId(sellerId)).thenReturn(Optional.empty());
        when(saveShippingPolicyPort.save(any(ShippingPolicy.class))).thenReturn(1L);

        RegisterShippingPolicyCommand command = new RegisterShippingPolicyCommand(
                "한진택배", 3000L, 20000L
        );

        // when
        RegisterShippingPolicyResult result = registerShippingPolicyService.registerShippingPolicy(sellerId, command);

        // then
        assertThat(result.id()).isEqualTo(1L);

        ArgumentCaptor<ShippingPolicy> captor = ArgumentCaptor.forClass(ShippingPolicy.class);
        verify(saveShippingPolicyPort).save(captor.capture());

        ShippingPolicy savedPolicy = captor.getValue();
        assertThat(savedPolicy.getSellerId()).isEqualTo(sellerId);
        assertThat(savedPolicy.getDeliveryCompany()).isEqualTo("한진택배");
        assertThat(savedPolicy.getShippingFee()).isEqualTo(3000L);
        assertThat(savedPolicy.getFreeShippingThreshold()).isEqualTo(20000L);
        assertThat(savedPolicy.isActive()).isTrue();
    }

    @Test
    @DisplayName("동일 판매자에 활성 배송비 정책이 이미 존재하면 예외를 던진다")
    void shouldThrowWhenShippingPolicyAlreadyExists() {
        // given
        Long sellerId = 10L;
        when(findShippingPolicyPort.findActiveBySellerId(sellerId))
                .thenReturn(Optional.of(createSavedPolicy(sellerId)));

        RegisterShippingPolicyCommand command = new RegisterShippingPolicyCommand(
                "한진택배", 3000L, 20000L
        );

        // when & then
        assertThatThrownBy(() -> registerShippingPolicyService.registerShippingPolicy(sellerId, command))
                .isInstanceOf(ShippingPolicyAlreadyExistsException.class);

        verify(saveShippingPolicyPort, never()).save(any());
    }

    @Test
    @DisplayName("배송비가 음수이면 예외를 던진다")
    void shouldThrowWhenShippingFeeIsNegative() {
        // given
        Long sellerId = 10L;
        when(findShippingPolicyPort.findActiveBySellerId(sellerId)).thenReturn(Optional.empty());

        RegisterShippingPolicyCommand command = new RegisterShippingPolicyCommand(
                "한진택배", -1L, 20000L
        );

        // when & then
        assertThatThrownBy(() -> registerShippingPolicyService.registerShippingPolicy(sellerId, command))
                .isInstanceOf(InvalidShippingFeeException.class);

        verify(saveShippingPolicyPort, never()).save(any());
    }

    @Test
    @DisplayName("무료배송 기준금액이 음수이면 예외를 던진다")
    void shouldThrowWhenFreeShippingThresholdIsNegative() {
        // given
        Long sellerId = 10L;
        when(findShippingPolicyPort.findActiveBySellerId(sellerId)).thenReturn(Optional.empty());

        RegisterShippingPolicyCommand command = new RegisterShippingPolicyCommand(
                "한진택배", 3000L, -1L
        );

        // when & then
        assertThatThrownBy(() -> registerShippingPolicyService.registerShippingPolicy(sellerId, command))
                .isInstanceOf(InvalidFreeShippingThresholdException.class);

        verify(saveShippingPolicyPort, never()).save(any());
    }

    @Test
    @DisplayName("배송비 정책 등록 성공 시 CREATED 이벤트를 발행한다")
    void shouldPublishCreatedEventWhenPolicyRegistered() {
        // given
        Long sellerId = 10L;
        when(findShippingPolicyPort.findActiveBySellerId(sellerId)).thenReturn(Optional.empty());
        when(saveShippingPolicyPort.save(any(ShippingPolicy.class))).thenReturn(1L);

        RegisterShippingPolicyCommand command = new RegisterShippingPolicyCommand(
                "한진택배", 3000L, 20000L
        );

        // when
        registerShippingPolicyService.registerShippingPolicy(sellerId, command);

        // then
        verify(publishShippingPolicyEventPort).publishShippingPolicyChangedEvent(
                sellerId, 3000L, 20000L, ShippingPolicyChangeAction.CREATED
        );
    }

    @Test
    @DisplayName("배송비 정책 등록 실패 시 이벤트를 발행하지 않는다")
    void shouldNotPublishEventWhenRegistrationFails() {
        // given
        Long sellerId = 10L;
        when(findShippingPolicyPort.findActiveBySellerId(sellerId))
                .thenReturn(Optional.of(createSavedPolicy(sellerId)));

        RegisterShippingPolicyCommand command = new RegisterShippingPolicyCommand(
                "한진택배", 3000L, 20000L
        );

        // when & then
        assertThatThrownBy(() -> registerShippingPolicyService.registerShippingPolicy(sellerId, command))
                .isInstanceOf(ShippingPolicyAlreadyExistsException.class);

        verifyNoInteractions(publishShippingPolicyEventPort);
    }
}
