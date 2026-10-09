package com.personal.marketnote.product.adapter.in.web.pricepolicy.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.product.adapter.in.web.pricepolicy.request.RegisterPricePolicyRequest;
import com.personal.marketnote.product.adapter.in.web.pricepolicy.response.GetPricePoliciesResponse;
import com.personal.marketnote.product.adapter.in.web.pricepolicy.response.RegisterPricePolicyResponse;
import com.personal.marketnote.product.port.in.command.RegisterPricePolicyCommand;
import com.personal.marketnote.product.port.in.result.pricepolicy.GetPricePoliciesResult;
import com.personal.marketnote.product.port.in.result.pricepolicy.RegisterPricePolicyResult;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.DeletePricePolicyUseCase;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.GetPricePoliciesUseCase;
import com.personal.marketnote.product.port.in.usecase.pricepolicy.RegisterPricePolicyUseCase;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PricePolicyController 테스트")
class PricePolicyControllerTest {

    @InjectMocks
    private PricePolicyController pricePolicyController;

    @Mock
    private RegisterPricePolicyUseCase registerPricePolicyUseCase;
    @Mock
    private GetPricePoliciesUseCase getPricePoliciesUseCase;
    @Mock
    private DeletePricePolicyUseCase deletePricePolicyUseCase;

    @Nested
    @DisplayName("POST /api/v1/products/{productId}/price-policies")
    class RegisterPricePolicy {

        @Test
        @DisplayName("상품 가격 정책 등록 시 201 CREATED를 반환한다")
        void returnsCreatedOnRegister() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();
            RegisterPricePolicyRequest request = buildRequest();
            RegisterPricePolicyResult result = RegisterPricePolicyResult.of(1L);
            when(registerPricePolicyUseCase.registerPricePolicy(
                    eq(100L), anyBoolean(), any(RegisterPricePolicyCommand.class)
            )).thenReturn(result);

            ResponseEntity<BaseResponse<RegisterPricePolicyResponse>> response =
                    pricePolicyController.registerPricePolicy(1L, request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{productId}/price-policies")
    class GetPricePolicies {

        @Test
        @DisplayName("상품 가격 정책 목록 조회 시 200 OK를 반환한다")
        void returnsOkOnGet() {
            GetPricePoliciesResult result = GetPricePoliciesResult.of(List.of());
            when(getPricePoliciesUseCase.getPricePoliciesAndOptions(1L)).thenReturn(result);

            ResponseEntity<BaseResponse<GetPricePoliciesResponse>> response =
                    pricePolicyController.getPricePolicies(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getPricePoliciesUseCase).getPricePoliciesAndOptions(1L);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{productId}/price-policies/{pricePolicyId}")
    class DeletePricePolicy {

        @Test
        @DisplayName("상품 가격 정책 삭제 시 200 OK를 반환한다")
        void returnsOkOnDelete() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();

            ResponseEntity<BaseResponse<Void>> response =
                    pricePolicyController.deletePricePolicy(1L, 5L, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deletePricePolicyUseCase).delete(eq(100L), anyBoolean(), eq(1L), eq(5L));
        }
    }

    private OAuth2AuthenticatedPrincipal mockPrincipal() {
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(principal.getName()).thenReturn("100");
        return principal;
    }

    private RegisterPricePolicyRequest buildRequest() {
        RegisterPricePolicyRequest request = new RegisterPricePolicyRequest();
        ReflectionTestUtils.setField(request, "price", 45000L);
        ReflectionTestUtils.setField(request, "discountPrice", 37000L);
        ReflectionTestUtils.setField(request, "accumulatedPoint", 1200L);
        ReflectionTestUtils.setField(request, "optionIds", List.of(3L, 7L));
        return request;
    }
}
