package com.personal.marketnote.product.adapter.in.web.product.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.product.adapter.in.web.product.response.GetProductKeyResponse;
import com.personal.marketnote.product.adapter.in.web.product.response.GetProductSearchTargetsResponse;
import com.personal.marketnote.product.adapter.in.web.product.response.GetProductSortPropertiesResponse;
import com.personal.marketnote.product.adapter.in.web.product.response.GetProductsResponse;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.port.in.command.DeleteProductCommand;
import com.personal.marketnote.product.port.in.command.DeleteProductImageCommand;
import com.personal.marketnote.product.port.in.result.product.GetProductKeyResult;
import com.personal.marketnote.product.port.in.result.product.GetProductSearchTargetsResult;
import com.personal.marketnote.product.port.in.result.product.GetProductSortPropertiesResult;
import com.personal.marketnote.product.port.in.result.product.GetProductsResult;
import com.personal.marketnote.product.port.in.usecase.product.DeleteProductImageUseCase;
import com.personal.marketnote.product.port.in.usecase.product.DeleteProductUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetAdminProductDetailUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetAdminProductsUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductKeyUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductSearchTargetsUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductSortPropertiesUseCase;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.in.usecase.product.RegisterProductUseCase;
import com.personal.marketnote.product.port.in.usecase.product.ReorderProductTagsUseCase;
import com.personal.marketnote.product.port.in.usecase.product.UpdateProductUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController 테스트")
class ProductControllerTest {

    @InjectMocks
    private ProductController productController;

    @Mock
    private RegisterProductUseCase registerProductUseCase;
    @Mock
    private GetProductSortPropertiesUseCase getProductSortPropertiesUseCase;
    @Mock
    private GetProductSearchTargetsUseCase getProductSearchTargetsUseCase;
    @Mock
    private GetProductUseCase getProductUseCase;
    @Mock
    private GetProductKeyUseCase getProductKeyUseCase;
    @Mock
    private GetAdminProductsUseCase getAdminProductsUseCase;
    @Mock
    private GetAdminProductDetailUseCase getAdminProductDetailUseCase;
    @Mock
    private UpdateProductUseCase updateProductUseCase;
    @Mock
    private DeleteProductUseCase deleteProductUseCase;
    @Mock
    private ReorderProductTagsUseCase reorderProductTagsUseCase;
    @Mock
    private DeleteProductImageUseCase deleteProductImageUseCase;

    @Nested
    @DisplayName("GET /api/v1/products/sort-properties")
    class GetProductSortProperties {

        @Test
        @DisplayName("상품 정렬 속성 목록 조회 시 200 OK와 정렬 속성 목록을 반환한다")
        void returnsOkWithSortProperties() {
            GetProductSortPropertiesResult result = GetProductSortPropertiesResult.from(ProductSortProperty.values());
            when(getProductSortPropertiesUseCase.getProductSortProperties()).thenReturn(result);

            ResponseEntity<BaseResponse<GetProductSortPropertiesResponse>> response =
                    productController.getProductSortProperties();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotNull();
            verify(getProductSortPropertiesUseCase).getProductSortProperties();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/search-targets")
    class GetProductSearchTargets {

        @Test
        @DisplayName("상품 검색 대상 목록 조회 시 200 OK와 검색 대상 목록을 반환한다")
        void returnsOkWithSearchTargets() {
            GetProductSearchTargetsResult result = GetProductSearchTargetsResult.from(ProductSearchTarget.values());
            when(getProductSearchTargetsUseCase.getProductSearchTargets()).thenReturn(result);

            ResponseEntity<BaseResponse<GetProductSearchTargetsResponse>> response =
                    productController.getProductSearchTargets();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(getProductSearchTargetsUseCase).getProductSearchTargets();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products")
    class GetProducts {

        @Test
        @DisplayName("상품 목록 조회 시 200 OK와 상품 목록을 반환한다")
        void returnsOkWithProducts() {
            GetProductsResult result = GetProductsResult.from(false, -1L, 0L, List.of());
            when(getProductUseCase.getProducts(
                    eq(1L), eq(List.of()), eq(-1L), eq(4),
                    eq(Sort.Direction.DESC), eq(ProductSortProperty.ORDER_NUM),
                    eq(ProductSearchTarget.NAME), eq("")
            )).thenReturn(result);

            ResponseEntity<BaseResponse<GetProductsResponse>> response = productController.getProducts(
                    1L, List.of(), -1L, 4,
                    Sort.Direction.DESC, ProductSortProperty.ORDER_NUM,
                    ProductSearchTarget.NAME, ""
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(getProductUseCase).getProducts(
                    eq(1L), eq(List.of()), eq(-1L), eq(4),
                    eq(Sort.Direction.DESC), eq(ProductSortProperty.ORDER_NUM),
                    eq(ProductSearchTarget.NAME), eq("")
            );
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}/product-key")
    class GetProductKey {

        @Test
        @DisplayName("상품 productKey 조회 시 200 OK와 UUID를 반환한다")
        void returnsOkWithProductKey() {
            UUID key = UUID.randomUUID();
            GetProductKeyResult result = new GetProductKeyResult(key);
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("100");
            when(getProductKeyUseCase.getProductKey(eq(1L), any(), anyBoolean())).thenReturn(result);

            ResponseEntity<BaseResponse<GetProductKeyResponse>> response =
                    productController.getProductKey(1L, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent().productKey()).isEqualTo(key);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{id}")
    class DeleteProduct {

        @Test
        @DisplayName("상품 삭제 시 200 OK를 반환하고 UseCase를 호출한다")
        void deletesProduct() {
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("100");

            ResponseEntity<BaseResponse<Void>> response = productController.deleteProduct(1L, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteProductUseCase).delete(any(), anyBoolean(), any(DeleteProductCommand.class));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{id}/images/{fileId}")
    class DeleteProductImage {

        @Test
        @DisplayName("상품 이미지 삭제 시 200 OK를 반환하고 UseCase를 호출한다")
        void deletesProductImage() {
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("100");

            ResponseEntity<BaseResponse<Void>> response = productController.deleteProductImage(1L, 2L, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(deleteProductImageUseCase).delete(any(), anyBoolean(), any(DeleteProductImageCommand.class));
        }
    }
}
