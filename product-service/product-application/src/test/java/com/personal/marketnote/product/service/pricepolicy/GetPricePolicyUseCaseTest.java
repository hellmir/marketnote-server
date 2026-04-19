package com.personal.marketnote.product.service.pricepolicy;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicy;
import com.personal.marketnote.product.domain.pricepolicy.PricePolicySnapshotState;
import com.personal.marketnote.product.exception.PricePolicyNotFoundException;
import com.personal.marketnote.product.port.out.pricepolicy.FindPricePolicyPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPricePolicyUseCaseTest {
    @Mock
    private FindPricePolicyPort findPricePolicyPort;

    @InjectMocks
    private GetPricePolicyService getPricePolicyService;

    @Test
    @DisplayName("가격 정책 ID로 조회하면 가격 정책을 반환한다")
    void getPricePolicy_byId_returnsPolicy() {
        Long id = 10L;
        PricePolicy policy = buildPricePolicy(id, List.of());

        when(findPricePolicyPort.findById(id)).thenReturn(Optional.of(policy));

        PricePolicy result = getPricePolicyService.getPricePolicy(id);

        assertThat(result).isSameAs(policy);
        verify(findPricePolicyPort).findById(id);
    }

    @Test
    @DisplayName("가격 정책 ID로 조회 시 없으면 예외를 던진다")
    void getPricePolicy_byId_notFound() {
        Long id = 20L;

        when(findPricePolicyPort.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getPricePolicyService.getPricePolicy(id))
                .isInstanceOf(PricePolicyNotFoundException.class)
                .hasMessageContaining(String.valueOf(id));

        verify(findPricePolicyPort).findById(id);
    }

    @Test
    @DisplayName("가격 정책 ID로 조회 시 포트 예외를 전파한다")
    void getPricePolicy_byId_portFails_propagates() {
        Long id = 30L;
        RuntimeException exception = new RuntimeException("find fail");

        when(findPricePolicyPort.findById(id)).thenThrow(exception);

        assertThatThrownBy(() -> getPricePolicyService.getPricePolicy(id))
                .isSameAs(exception);
    }

    @Test
    @DisplayName("옵션 ID 목록으로 조회하면 가격 정책을 반환한다")
    void getPricePolicy_byOptionIds_returnsPolicy() {
        List<Long> optionIds = List.of(101L, 102L);
        PricePolicy policy = buildPricePolicy(100L, optionIds);

        when(findPricePolicyPort.findByOptionIds(optionIds)).thenReturn(Optional.of(policy));

        PricePolicy result = getPricePolicyService.getPricePolicy(optionIds);

        assertThat(result).isSameAs(policy);
        verify(findPricePolicyPort).findByOptionIds(optionIds);
    }

    @Test
    @DisplayName("옵션 ID 목록으로 조회 시 튜플이 존재하지 않으면 예외를 던진다")
    void getPricePolicy_byOptionIds_notFound() {
        List<Long> optionIds = List.of(201L);

        when(findPricePolicyPort.findByOptionIds(optionIds)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getPricePolicyService.getPricePolicy(optionIds))
                .isInstanceOf(PricePolicyNotFoundException.class)
                .hasMessageContaining(String.valueOf(-1L));

        verify(findPricePolicyPort).findByOptionIds(optionIds);
    }

    @Test
    @DisplayName("옵션 ID 목록으로 조회 시 포트 예외를 전파한다")
    void getPricePolicy_byOptionIds_portFails_propagates() {
        List<Long> optionIds = List.of(301L);
        RuntimeException exception = new RuntimeException("option find fail");

        when(findPricePolicyPort.findByOptionIds(optionIds)).thenThrow(exception);

        assertThatThrownBy(() -> getPricePolicyService.getPricePolicy(optionIds))
                .isSameAs(exception);
    }

    private PricePolicy buildPricePolicy(Long id, List<Long> optionIds) {
        return PricePolicy.from(
                PricePolicySnapshotState.builder()
                        .id(id)
                        .price(10000L)
                        .discountPrice(8000L)
                        .discountRate(new BigDecimal("20.0"))
                        .accumulatedPoint(200L)
                        .accumulationRate(new BigDecimal("2.5"))
                        .status(EntityStatus.ACTIVE)
                        .optionIds(optionIds)
                        .build()
        );
    }
}
