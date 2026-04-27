package com.personal.marketnote.product.adapter.in.web.shipping.controller;

import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyBySellerResult;
import com.personal.marketnote.product.port.in.usecase.shipping.GetShippingPolicyUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("InternalShippingPolicyController 테스트")
class InternalShippingPolicyControllerTest {
    @InjectMocks
    private InternalShippingPolicyController internalShippingPolicyController;

    @Mock
    private GetShippingPolicyUseCase getShippingPolicyUseCase;

    @Nested
    @DisplayName("getShippingPoliciesBySellerIds")
    class GetShippingPoliciesBySellerIds {
        @Test
        @DisplayName("판매자 ID 목록으로 배송비 정책을 조회하면 200 OK를 반환한다")
        void shouldReturnOkWhenGetShippingPoliciesBySellerIdsSucceeds() {
            // given
            List<Long> sellerIds = List.of(10L, 20L);
            List<GetShippingPolicyBySellerResult> results = List.of(
                    new GetShippingPolicyBySellerResult(10L, 3000L, 20000L),
                    new GetShippingPolicyBySellerResult(20L, 2500L, 30000L)
            );
            when(getShippingPolicyUseCase.getShippingPolicies(sellerIds)).thenReturn(results);

            // when
            ResponseEntity<?> response = internalShippingPolicyController.getShippingPoliciesBySellerIds(sellerIds);

            // then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getShippingPolicyUseCase).getShippingPolicies(sellerIds);
        }
    }
}
