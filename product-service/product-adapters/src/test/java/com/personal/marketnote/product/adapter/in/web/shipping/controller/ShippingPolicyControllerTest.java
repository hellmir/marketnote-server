package com.personal.marketnote.product.adapter.in.web.shipping.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.product.adapter.in.web.shipping.request.RegisterShippingPolicyRequest;
import com.personal.marketnote.product.adapter.in.web.shipping.request.UpdateShippingPolicyRequest;
import com.personal.marketnote.product.adapter.in.web.shipping.response.GetShippingPoliciesBySellerIdsResponse;
import com.personal.marketnote.product.adapter.in.web.shipping.response.GetShippingPolicyResponse;
import com.personal.marketnote.product.adapter.in.web.shipping.response.RegisterShippingPolicyResponse;
import com.personal.marketnote.product.adapter.in.web.shipping.response.UpdateShippingPolicyResponse;
import com.personal.marketnote.product.port.in.command.RegisterShippingPolicyCommand;
import com.personal.marketnote.product.port.in.command.UpdateShippingPolicyCommand;
import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyBySellerResult;
import com.personal.marketnote.product.port.in.result.shipping.GetShippingPolicyResult;
import com.personal.marketnote.product.port.in.result.shipping.RegisterShippingPolicyResult;
import com.personal.marketnote.product.port.in.result.shipping.UpdateShippingPolicyResult;
import com.personal.marketnote.product.port.in.usecase.shipping.GetShippingPolicyUseCase;
import com.personal.marketnote.product.port.in.usecase.shipping.RegisterShippingPolicyUseCase;
import com.personal.marketnote.product.port.in.usecase.shipping.UpdateShippingPolicyUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShippingPolicyController 테스트")
class ShippingPolicyControllerTest {

    @InjectMocks
    private ShippingPolicyController shippingPolicyController;

    @Mock
    private GetShippingPolicyUseCase getShippingPolicyUseCase;
    @Mock
    private RegisterShippingPolicyUseCase registerShippingPolicyUseCase;
    @Mock
    private UpdateShippingPolicyUseCase updateShippingPolicyUseCase;

    @Nested
    @DisplayName("GET /api/v1/shipping-policies")
    class GetShippingPolicy {

        @Test
        @DisplayName("판매자 배송비 정책 조회 시 200 OK를 반환한다")
        void returnsOkOnGet() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();
            GetShippingPolicyResult result = new GetShippingPolicyResult(1L, "한진택배", 3000L, 30000L, 3000L, 5000L);
            when(getShippingPolicyUseCase.getShippingPolicy(100L)).thenReturn(result);

            ResponseEntity<BaseResponse<GetShippingPolicyResponse>> response =
                    shippingPolicyController.getShippingPolicy(principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getShippingPolicyUseCase).getShippingPolicy(100L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/shipping-policies/sellers")
    class GetShippingPoliciesBySellerIds {

        @Test
        @DisplayName("판매자별 배송비 정책 배치 조회 시 200 OK를 반환한다")
        void returnsOkOnBatchGet() {
            List<Long> sellerIds = List.of(10L, 20L);
            List<GetShippingPolicyBySellerResult> results = List.of(
                    new GetShippingPolicyBySellerResult(10L, 3000L, 30000L, 3000L, 5000L)
            );
            when(getShippingPolicyUseCase.getShippingPolicies(sellerIds)).thenReturn(results);

            ResponseEntity<BaseResponse<GetShippingPoliciesBySellerIdsResponse>> response =
                    shippingPolicyController.getShippingPoliciesBySellerIds(sellerIds);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/shipping-policies")
    class RegisterShippingPolicy {

        @Test
        @DisplayName("배송비 정책 등록 시 201 CREATED를 반환한다")
        void returnsCreatedOnRegister() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();
            RegisterShippingPolicyRequest request = new RegisterShippingPolicyRequest(
                    "한진택배", 3000L, 30000L, 3000L, 5000L
            );
            when(registerShippingPolicyUseCase.registerShippingPolicy(eq(100L), any(RegisterShippingPolicyCommand.class)))
                    .thenReturn(RegisterShippingPolicyResult.of(1L));

            ResponseEntity<BaseResponse<RegisterShippingPolicyResponse>> response =
                    shippingPolicyController.registerShippingPolicy(request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/shipping-policies")
    class UpdateShippingPolicy {

        @Test
        @DisplayName("배송비 정책 수정 시 200 OK를 반환한다")
        void returnsOkOnUpdate() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();
            UpdateShippingPolicyRequest request = new UpdateShippingPolicyRequest(
                    "한진택배", 3500L, 30000L, 3000L, 5000L
            );
            UpdateShippingPolicyResult result = new UpdateShippingPolicyResult(
                    1L, "한진택배", 3500L, 30000L, 3000L, 5000L
            );
            when(updateShippingPolicyUseCase.updateShippingPolicy(eq(100L), any(UpdateShippingPolicyCommand.class)))
                    .thenReturn(result);

            ResponseEntity<BaseResponse<UpdateShippingPolicyResponse>> response =
                    shippingPolicyController.updateShippingPolicy(request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    private OAuth2AuthenticatedPrincipal mockPrincipal() {
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(principal.getName()).thenReturn("100");
        return principal;
    }
}
