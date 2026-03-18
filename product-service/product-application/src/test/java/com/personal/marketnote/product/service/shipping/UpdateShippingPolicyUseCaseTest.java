package com.personal.marketnote.product.service.shipping;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.shipping.InvalidFreeShippingThresholdException;
import com.personal.marketnote.product.domain.shipping.InvalidShippingFeeException;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.domain.shipping.ShippingPolicySnapshotState;
import com.personal.marketnote.product.exception.ShippingPolicyNotFoundException;
import com.personal.marketnote.product.port.in.command.UpdateShippingPolicyCommand;
import com.personal.marketnote.product.port.in.result.shipping.UpdateShippingPolicyResult;
import com.personal.marketnote.product.port.out.shipping.FindShippingPolicyPort;
import com.personal.marketnote.product.port.out.shipping.UpdateShippingPolicyPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateShippingPolicyUseCase 테스트")
class UpdateShippingPolicyUseCaseTest {

    @InjectMocks
    private UpdateShippingPolicyService updateShippingPolicyService;

    @Mock
    private FindShippingPolicyPort findShippingPolicyPort;

    @Mock
    private UpdateShippingPolicyPort updateShippingPolicyPort;

    private ShippingPolicy createExistingPolicy(Long sellerId) {
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
    @DisplayName("배송비 정책을 정상 수정한다")
    void shouldUpdateShippingPolicySuccessfully() {
        // given
        Long sellerId = 10L;
        ShippingPolicy existingPolicy = createExistingPolicy(sellerId);
        when(findShippingPolicyPort.findActiveBySellerId(sellerId))
                .thenReturn(Optional.of(existingPolicy));

        UpdateShippingPolicyCommand command = new UpdateShippingPolicyCommand(
                "CJ대한통운", 2500L, 30000L
        );

        // when
        UpdateShippingPolicyResult result = updateShippingPolicyService.updateShippingPolicy(sellerId, command);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.deliveryCompany()).isEqualTo("CJ대한통운");
        assertThat(result.shippingFee()).isEqualTo(2500L);
        assertThat(result.freeShippingThreshold()).isEqualTo(30000L);

        verify(updateShippingPolicyPort).update(existingPolicy);
    }

    @Test
    @DisplayName("해당 판매자의 활성 배송비 정책이 없으면 ShippingPolicyNotFoundException을 던진다")
    void shouldThrowWhenShippingPolicyNotFound() {
        // given
        Long sellerId = 10L;
        when(findShippingPolicyPort.findActiveBySellerId(sellerId))
                .thenReturn(Optional.empty());

        UpdateShippingPolicyCommand command = new UpdateShippingPolicyCommand(
                "CJ대한통운", 2500L, 30000L
        );

        // when & then
        assertThatThrownBy(() -> updateShippingPolicyService.updateShippingPolicy(sellerId, command))
                .isInstanceOf(ShippingPolicyNotFoundException.class);

        verify(updateShippingPolicyPort, never()).update(any());
    }

    @Test
    @DisplayName("배송비가 음수이면 InvalidShippingFeeException을 던진다")
    void shouldThrowWhenShippingFeeIsNegative() {
        // given
        Long sellerId = 10L;
        ShippingPolicy existingPolicy = createExistingPolicy(sellerId);
        when(findShippingPolicyPort.findActiveBySellerId(sellerId))
                .thenReturn(Optional.of(existingPolicy));

        UpdateShippingPolicyCommand command = new UpdateShippingPolicyCommand(
                "CJ대한통운", -1L, 30000L
        );

        // when & then
        assertThatThrownBy(() -> updateShippingPolicyService.updateShippingPolicy(sellerId, command))
                .isInstanceOf(InvalidShippingFeeException.class);

        verify(updateShippingPolicyPort, never()).update(any());
    }

    @Test
    @DisplayName("무료배송 기준금액이 음수이면 InvalidFreeShippingThresholdException을 던진다")
    void shouldThrowWhenFreeShippingThresholdIsNegative() {
        // given
        Long sellerId = 10L;
        ShippingPolicy existingPolicy = createExistingPolicy(sellerId);
        when(findShippingPolicyPort.findActiveBySellerId(sellerId))
                .thenReturn(Optional.of(existingPolicy));

        UpdateShippingPolicyCommand command = new UpdateShippingPolicyCommand(
                "CJ대한통운", 2500L, -1L
        );

        // when & then
        assertThatThrownBy(() -> updateShippingPolicyService.updateShippingPolicy(sellerId, command))
                .isInstanceOf(InvalidFreeShippingThresholdException.class);

        verify(updateShippingPolicyPort, never()).update(any());
    }
}
