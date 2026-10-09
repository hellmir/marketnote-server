package com.personal.marketnote.product.adapter.in.web.option.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.product.adapter.in.web.option.request.RegisterProductOptionRequest;
import com.personal.marketnote.product.adapter.in.web.option.request.UpdateProductOptionsRequest;
import com.personal.marketnote.product.adapter.in.web.option.response.GetProductOptionsResponse;
import com.personal.marketnote.product.adapter.in.web.option.response.UpsertProductOptionsResponse;
import com.personal.marketnote.product.port.in.command.RegisterProductOptionsCommand;
import com.personal.marketnote.product.port.in.command.UpdateProductOptionsCommand;
import com.personal.marketnote.product.port.in.result.option.GetProductOptionsResult;
import com.personal.marketnote.product.port.in.result.option.UpdateProductOptionsResult;
import com.personal.marketnote.product.port.in.usecase.option.DeleteProductOptionsUseCase;
import com.personal.marketnote.product.port.in.usecase.option.GetProductOptionsUseCase;
import com.personal.marketnote.product.port.in.usecase.option.RegisterProductOptionsUseCase;
import com.personal.marketnote.product.port.in.usecase.option.UpdateProductOptionsUseCase;
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
@DisplayName("ProductOptionController 테스트")
class ProductOptionControllerTest {

    @InjectMocks
    private ProductOptionController productOptionController;

    @Mock
    private GetProductOptionsUseCase getProductOptionsUseCase;
    @Mock
    private RegisterProductOptionsUseCase registerProductOptionsUseCase;
    @Mock
    private UpdateProductOptionsUseCase updateProductOptionsUseCase;
    @Mock
    private DeleteProductOptionsUseCase deleteProductOptionsUseCase;

    @Nested
    @DisplayName("POST /api/v1/products/{productId}/option-categories")
    class RegisterProductOptionCategories {

        @Test
        @DisplayName("상품 옵션 카테고리 등록 시 201 CREATED를 반환한다")
        void returnsCreatedOnRegister() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();
            UpdateProductOptionsRequest request = buildRequest();
            UpdateProductOptionsResult result = new UpdateProductOptionsResult(1L, List.of(10L, 20L));
            when(registerProductOptionsUseCase.registerProductOptions(
                    eq(100L), anyBoolean(), any(RegisterProductOptionsCommand.class)
            )).thenReturn(result);

            ResponseEntity<BaseResponse<UpsertProductOptionsResponse>> response =
                    productOptionController.registerProductOptionCategories(1L, request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{productId}/option-categories")
    class GetProductOptions {

        @Test
        @DisplayName("옵션 카테고리 및 옵션 목록 조회 시 200 OK를 반환한다")
        void returnsOkOnGet() {
            GetProductOptionsResult result = new GetProductOptionsResult(List.of());
            when(getProductOptionsUseCase.getProductOptions(1L)).thenReturn(result);

            ResponseEntity<BaseResponse<GetProductOptionsResponse>> response =
                    productOptionController.getProductOptions(1L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getProductOptionsUseCase).getProductOptions(1L);
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products/{productId}/option-categories/{id}")
    class UpdateProductOptionCategories {

        @Test
        @DisplayName("상품 옵션 카테고리 수정 시 200 OK를 반환한다")
        void returnsOkOnUpdate() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();
            UpdateProductOptionsRequest request = buildRequest();
            UpdateProductOptionsResult result = new UpdateProductOptionsResult(1L, List.of(10L, 20L));
            when(updateProductOptionsUseCase.updateProductOptions(
                    eq(100L), anyBoolean(), any(UpdateProductOptionsCommand.class)
            )).thenReturn(result);

            ResponseEntity<BaseResponse<UpsertProductOptionsResponse>> response =
                    productOptionController.updateProductOptionCategories(1L, 5L, request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{productId}/option-categories/{id}")
    class DeleteProductOptionCategories {

        @Test
        @DisplayName("상품 옵션 카테고리 삭제 시 200 OK를 반환한다")
        void returnsOkOnDelete() {
            OAuth2AuthenticatedPrincipal principal = mockPrincipal();

            ResponseEntity<BaseResponse<Void>> response =
                    productOptionController.deleteProductOptionCategories(1L, 5L, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteProductOptionsUseCase).deleteProductOptions(eq(100L), anyBoolean(), eq(1L), eq(5L));
        }
    }

    private OAuth2AuthenticatedPrincipal mockPrincipal() {
        OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
        when(principal.getName()).thenReturn("100");
        return principal;
    }

    private UpdateProductOptionsRequest buildRequest() {
        UpdateProductOptionsRequest request = new UpdateProductOptionsRequest();
        ReflectionTestUtils.setField(request, "categoryName", "색상");
        ReflectionTestUtils.setField(request, "options", List.of(buildOption("빨강"), buildOption("파랑")));
        return request;
    }

    private RegisterProductOptionRequest buildOption(String content) {
        RegisterProductOptionRequest option = new RegisterProductOptionRequest();
        ReflectionTestUtils.setField(option, "content", content);
        return option;
    }
}
