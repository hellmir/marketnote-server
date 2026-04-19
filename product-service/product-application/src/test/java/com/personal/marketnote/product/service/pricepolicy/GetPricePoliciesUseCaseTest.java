package com.personal.marketnote.product.service.pricepolicy;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetPricePoliciesResult;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetProductPricePolicyWithOptionsResult;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePoliciesPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPricePoliciesUseCaseTest {
    @Mock
    private FindPricePoliciesPort findPricePoliciesPort;

    @InjectMocks
    private GetPricePoliciesService getPricePoliciesService;

    @Test
    @DisplayName("상품 ID로 가격 정책 및 옵션 목록을 조회하면 매핑된 결과를 반환한다")
    void getPricePoliciesAndOptions_byProductId_mapsResult() {
        Long productId = 10L;
        PricePolicy policy1 = buildPricePolicy(100L, 10000L, 8000L, 200L, new BigDecimal("20.0"), List.of());
        PricePolicy policy2 = buildPricePolicy(200L, 15000L, 12000L, 300L, new BigDecimal("20.0"), List.of(1L, 2L));

        when(findPricePoliciesPort.findByProductId(productId)).thenReturn(List.of(policy1, policy2));

        GetPricePoliciesResult result = getPricePoliciesService.getPricePoliciesAndOptions(productId);

        assertThat(result.policies()).hasSize(2);
        GetProductPricePolicyWithOptionsResult item1 = result.policies().getFirst();
        GetProductPricePolicyWithOptionsResult item2 = result.policies().getLast();

        assertThat(item1.id()).isEqualTo(policy1.getId());
        assertThat(item1.price()).isEqualTo(policy1.getPrice());
        assertThat(item1.discountPrice()).isEqualTo(policy1.getDiscountPrice());
        assertThat(item1.accumulatedPoint()).isEqualTo(policy1.getAccumulatedPoint());
        assertThat(item1.discountRate()).isEqualTo(policy1.getDiscountRate());
        assertThat(item1.optionIds()).containsExactlyElementsOf(policy1.getOptionIds());

        assertThat(item2.id()).isEqualTo(policy2.getId());
        assertThat(item2.price()).isEqualTo(policy2.getPrice());
        assertThat(item2.discountPrice()).isEqualTo(policy2.getDiscountPrice());
        assertThat(item2.accumulatedPoint()).isEqualTo(policy2.getAccumulatedPoint());
        assertThat(item2.discountRate()).isEqualTo(policy2.getDiscountRate());
        assertThat(item2.optionIds()).containsExactlyElementsOf(policy2.getOptionIds());
    }

    @Test
    @DisplayName("상품 ID로 가격 정책 및 옵션 목록을 조회할 때 결과가 없으면 빈 목록을 반환한다")
    void getPricePoliciesAndOptions_byProductId_empty() {
        Long productId = 11L;

        when(findPricePoliciesPort.findByProductId(productId)).thenReturn(List.of());

        GetPricePoliciesResult result = getPricePoliciesService.getPricePoliciesAndOptions(productId);

        assertThat(result.policies()).isEmpty();
        verify(findPricePoliciesPort).findByProductId(productId);
    }

    @Test
    @DisplayName("상품 ID로 가격 정책 및 옵션 목록을 조회할 때 포트 예외를 전파한다")
    void getPricePoliciesAndOptions_byProductId_portFails_propagates() {
        Long productId = 12L;
        RuntimeException exception = new RuntimeException("find fail");

        when(findPricePoliciesPort.findByProductId(productId)).thenThrow(exception);

        assertThatThrownBy(() -> getPricePoliciesService.getPricePoliciesAndOptions(productId))
                .isSameAs(exception);
    }

    @Test
    @DisplayName("가격 정책 ID 목록으로 조회하면 포트 결과를 그대로 반환한다")
    void getPricePoliciesAndOptions_byIds_returnsPolicies() {
        List<Long> ids = List.of(101L, 102L);
        List<PricePolicy> policies = List.of(
                buildPricePolicy(101L, 10000L, 8000L, 200L, new BigDecimal("20.0"), List.of()),
                buildPricePolicy(102L, 12000L, 9000L, 250L, new BigDecimal("25.0"), List.of(10L))
        );

        when(findPricePoliciesPort.findByIds(ids)).thenReturn(policies);

        List<PricePolicy> result = getPricePoliciesService.getPricePoliciesAndOptions(ids);

        assertThat(result).isSameAs(policies);
        verify(findPricePoliciesPort).findByIds(ids);
    }

    @Test
    @DisplayName("가격 정책 ID 목록으로 조회할 때 결과가 없으면 빈 목록을 반환한다")
    void getPricePoliciesAndOptions_byIds_empty() {
        List<Long> ids = List.of(201L);

        when(findPricePoliciesPort.findByIds(ids)).thenReturn(List.of());

        List<PricePolicy> result = getPricePoliciesService.getPricePoliciesAndOptions(ids);

        assertThat(result).isEmpty();
        verify(findPricePoliciesPort).findByIds(ids);
    }

    @Test
    @DisplayName("가격 정책 ID 목록으로 조회할 때 포트 예외를 전파한다")
    void getPricePoliciesAndOptions_byIds_portFails_propagates() {
        List<Long> ids = List.of(301L);
        RuntimeException exception = new RuntimeException("find fail");

        when(findPricePoliciesPort.findByIds(ids)).thenThrow(exception);

        assertThatThrownBy(() -> getPricePoliciesService.getPricePoliciesAndOptions(ids))
                .isSameAs(exception);
    }

    private PricePolicy buildPricePolicy(
            Long id,
            Long price,
            Long discountPrice,
            Long accumulatedPoint,
            BigDecimal discountRate,
            List<Long> optionIds
    ) {
        return PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(id)
                        .price(price)
                        .discountPrice(discountPrice)
                        .discountRate(discountRate)
                        .accumulatedPoint(accumulatedPoint)
                        .status(EntityStatus.ACTIVE)
                        .optionIds(optionIds)
                        .build()
        );
    }
}
