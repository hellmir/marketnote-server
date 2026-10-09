package com.personal.marketnote.product.adapter.in.web.cart.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.product.adapter.in.web.cart.request.AddCartProductRequest;
import com.personal.marketnote.product.adapter.in.web.cart.request.UpdateCartProductOptionsRequest;
import com.personal.marketnote.product.adapter.in.web.cart.request.UpdateCartProductQuantityRequest;
import com.personal.marketnote.product.adapter.in.web.cart.response.GetMyCartProductsResponse;
import com.personal.marketnote.product.port.in.command.AddCartProductCommand;
import com.personal.marketnote.product.port.in.command.DeleteCartProductCommand;
import com.personal.marketnote.product.port.in.command.UpdateCartProductOptionCommand;
import com.personal.marketnote.product.port.in.command.UpdateCartProductQuantityCommand;
import com.personal.marketnote.product.port.in.result.cart.GetMyCartProductsResult;
import com.personal.marketnote.product.port.in.usecase.cart.AddCartProductUseCase;
import com.personal.marketnote.product.port.in.usecase.cart.DeleteCartProductUseCase;
import com.personal.marketnote.product.port.in.usecase.cart.GetCartProductUseCase;
import com.personal.marketnote.product.port.in.usecase.cart.UpdateCartProductOptionsUseCase;
import com.personal.marketnote.product.port.in.usecase.cart.UpdateCartProductQuantityUseCase;
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
@DisplayName("CartController 테스트")
class CartControllerTest {

    @InjectMocks
    private CartController cartController;

    @Mock
    private AddCartProductUseCase addCartProductUseCase;
    @Mock
    private GetCartProductUseCase getCartProductUseCase;
    @Mock
    private UpdateCartProductQuantityUseCase updateCartProductQuantityUseCase;
    @Mock
    private UpdateCartProductOptionsUseCase updateCartProductOptionsUseCase;
    @Mock
    private DeleteCartProductUseCase deleteCartProductsUseCase;

    @Nested
    @DisplayName("POST /api/v1/cart/products")
    class AddCartProduct {

        @Test
        @DisplayName("장바구니 상품 추가 시 201 CREATED를 반환한다")
        void returnsCreatedOnAdd() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();
            AddCartProductRequest request = new AddCartProductRequest(1L, null, "http://img", (short) 2);

            ResponseEntity<BaseResponse<Void>> response = cartController.addCartProduct(request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            verify(addCartProductUseCase).addCartProduct(any(AddCartProductCommand.class));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cart/products")
    class GetMyCartProducts {

        @Test
        @DisplayName("회원 장바구니 상품 목록 조회 시 200 OK를 반환한다")
        void returnsOkWithCartProducts() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();
            GetMyCartProductsResult result = new GetMyCartProductsResult(List.of());
            when(getCartProductUseCase.getMyCartProducts(eq(100L))).thenReturn(result);

            ResponseEntity<BaseResponse<GetMyCartProductsResponse>> response =
                    cartController.getMyCartProducts(principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent()).isNotNull();
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cart/products/quantity")
    class UpdateCartProductQuantity {

        @Test
        @DisplayName("장바구니 상품 수량 변경 시 200 OK를 반환한다")
        void returnsOkOnUpdateQuantity() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();
            UpdateCartProductQuantityRequest request = new UpdateCartProductQuantityRequest(1L, (short) 5);

            ResponseEntity<BaseResponse<Void>> response =
                    cartController.updateCartProductQuantity(request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(updateCartProductQuantityUseCase)
                    .updateCartProductQuantity(any(UpdateCartProductQuantityCommand.class));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cart/products/options")
    class UpdateCartProductOptions {

        @Test
        @DisplayName("장바구니 상품 옵션 변경 시 200 OK를 반환한다")
        void returnsOkOnUpdateOptions() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();
            UpdateCartProductOptionsRequest request = new UpdateCartProductOptionsRequest(1L, List.of(2L, 3L));

            ResponseEntity<BaseResponse<Void>> response =
                    cartController.updateCartProductOptions(request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(updateCartProductOptionsUseCase)
                    .updateCartProductOptions(any(UpdateCartProductOptionCommand.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/cart/products")
    class DeleteCartProducts {

        @Test
        @DisplayName("장바구니 상품 삭제 시 200 OK를 반환한다")
        void returnsOkOnDelete() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();

            ResponseEntity<BaseResponse<Void>> response =
                    cartController.deleteCartProducts(List.of(1L, 2L), principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteCartProductsUseCase).deleteCartProducts(any(DeleteCartProductCommand.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/cart")
    class DeleteAllCartProducts {

        @Test
        @DisplayName("장바구니 비우기 시 200 OK를 반환한다")
        void returnsOkOnClear() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();

            ResponseEntity<BaseResponse<Void>> response = cartController.deleteAllCartProducts(principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteCartProductsUseCase).deleteAllCartProducts(100L);
        }
    }

    private OAuth2AuthenticatedPrincipal mockPrincipal() {
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(principal.getName()).thenReturn("100");
        return principal;
    }
}
