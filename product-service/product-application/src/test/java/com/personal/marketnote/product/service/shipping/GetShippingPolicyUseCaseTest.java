package com.personal.marketnote.product.service.shipping;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.shipping.ShippingPolicy;
import com.personal.marketnote.product.domain.shipping.ShippingPolicySnapshotState;
import com.personal.marketnote.product.exception.ShippingPolicyNotFoundException;
import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyResult;
import com.personal.marketnote.product.port.out.shipping.FindShippingPolicyPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetShippingPolicyUseCase 테스트")
class GetShippingPolicyUseCaseTest {

    @InjectMocks
    private GetShippingPolicyService getShippingPolicyService;

    @Mock
    private FindShippingPolicyPort findShippingPolicyPort;

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
    @DisplayName("판매자의 활성 배송비 정책을 정상 조회한다")
    void shouldGetShippingPolicySuccessfully() {
        // given
        Long sellerId = 10L;
        ShippingPolicy existingPolicy = createExistingPolicy(sellerId);
        when(findShippingPolicyPort.findActiveBySellerId(sellerId))
                .thenReturn(Optional.of(existingPolicy));

        // when
        GetShippingPolicyResult result = getShippingPolicyService.getShippingPolicy(sellerId);

        // then
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.deliveryCompany()).isEqualTo("한진택배");
        assertThat(result.shippingFee()).isEqualTo(3000L);
        assertThat(result.freeShippingThreshold()).isEqualTo(20000L);

        verify(findShippingPolicyPort).findActiveBySellerId(sellerId);
    }

    @Test
    @DisplayName("해당 판매자의 활성 배송비 정책이 없으면 ShippingPolicyNotFoundException을 던진다")
    void shouldThrowWhenShippingPolicyNotFound() {
        // given
        Long sellerId = 10L;
        when(findShippingPolicyPort.findActiveBySellerId(sellerId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getShippingPolicyService.getShippingPolicy(sellerId))
                .isInstanceOf(ShippingPolicyNotFoundException.class);

        verify(findShippingPolicyPort).findActiveBySellerId(sellerId);
    }

    @Test
    @DisplayName("getShippingPolicies는 sellerIds가 null이면 빈 리스트를 반환하고 포트를 호출하지 않는다")
    void shouldReturnEmptyListWhenSellerIdsNull() {
        assertThat(getShippingPolicyService.getShippingPolicies(null)).isEmpty();

        verify(findShippingPolicyPort, never()).findActiveBySellerIds(List.of());
    }

    @Test
    @DisplayName("getShippingPolicies는 sellerIds가 비어 있으면 빈 리스트를 반환하고 포트를 호출하지 않는다")
    void shouldReturnEmptyListWhenSellerIdsEmpty() {
        assertThat(getShippingPolicyService.getShippingPolicies(List.of())).isEmpty();

        verify(findShippingPolicyPort, never()).findActiveBySellerIds(List.of());
    }

    @Test
    @DisplayName("getShippingPolicies는 조회된 정책 수만큼 결과를 매핑하여 반환한다")
    void shouldMapPoliciesToResults() {
        when(findShippingPolicyPort.findActiveBySellerIds(List.of(10L, 11L)))
                .thenReturn(List.of(createExistingPolicy(10L), createExistingPolicy(11L)));

        List<?> results = getShippingPolicyService.getShippingPolicies(List.of(10L, 11L));

        assertThat(results).hasSize(2);
    }
}
