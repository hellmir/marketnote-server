package com.personal.marketnote.product.adapter.in.web.product.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.in.ratelimit.RateLimited;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.product.adapter.in.web.product.request.RegisterProductRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.ReorderProductTagsRequest;
import com.personal.marketnote.product.adapter.in.web.product.request.UpdateProductRequest;
import com.personal.marketnote.product.adapter.in.web.product.response.GetAdminProductDetailResponse;
import com.personal.marketnote.product.adapter.in.web.product.response.GetAdminProductsResponse;
import com.personal.marketnote.product.adapter.in.web.product.response.GetProductInfoResponse;
import com.personal.marketnote.product.adapter.in.web.product.response.GetProductKeyResponse;
import com.personal.marketnote.product.adapter.in.web.product.response.GetProductSearchTargetsResponse;
import com.personal.marketnote.product.adapter.in.web.product.response.GetProductSortPropertiesResponse;
import com.personal.marketnote.product.adapter.in.web.product.response.GetProductsResponse;
import com.personal.marketnote.product.adapter.in.web.product.response.RegisterProductResponse;
import com.personal.marketnote.product.adapter.in.web.product.response.UpdateProductResponse;
import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.port.in.command.DeleteProductCommand;
import com.personal.marketnote.product.port.in.command.DeleteProductImageCommand;
import com.personal.marketnote.product.port.in.command.RegisterProductCommand;
import com.personal.marketnote.product.port.in.command.ReorderProductTagsCommand;
import com.personal.marketnote.product.port.in.command.UpdateProductCommand;
import com.personal.marketnote.product.port.in.result.product.GetAdminProductDetailResult;
import com.personal.marketnote.product.port.in.result.product.GetAdminProductsResult;
import com.personal.marketnote.product.port.in.result.product.GetProductInfoWithOptionsResult;
import com.personal.marketnote.product.port.in.result.product.GetProductKeyResult;
import com.personal.marketnote.product.port.in.result.product.GetProductSearchTargetsResult;
import com.personal.marketnote.product.port.in.result.product.GetProductSortPropertiesResult;
import com.personal.marketnote.product.port.in.result.product.GetProductsResult;
import com.personal.marketnote.product.port.in.result.product.RegisterProductResult;
import com.personal.marketnote.product.port.in.result.product.UpdateProductResult;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.lang.reflect.Method;
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

    @Nested
    @DisplayName("POST /api/v1/products")
    class RegisterProduct {

        @Test
        @DisplayName("상품 등록 시 201 CREATED와 등록 결과를 반환하고 UseCase에 매핑된 커맨드를 전달한다")
        void returnsCreatedWithResultAndInvokesUseCase() {
            RegisterProductRequest request = new RegisterProductRequest(
                    10L, "상품명", "브랜드", "상세",
                    10_000L, 8_000L, 100L, true,
                    List.of("tag1"), null
            );
            UUID productKey = UUID.randomUUID();
            RegisterProductResult result = new RegisterProductResult(1L, productKey);
            when(registerProductUseCase.registerProduct(any(RegisterProductCommand.class))).thenReturn(result);

            ResponseEntity<BaseResponse<RegisterProductResponse>> response =
                    productController.registerProduct(request);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().id()).isEqualTo(1L);
            assertThat(response.getBody().getContent().productKey()).isEqualTo(productKey);

            ArgumentCaptor<RegisterProductCommand> captor = ArgumentCaptor.forClass(RegisterProductCommand.class);
            verify(registerProductUseCase).registerProduct(captor.capture());
            RegisterProductCommand captured = captor.getValue();
            assertThat(captured.sellerId()).isEqualTo(10L);
            assertThat(captured.name()).isEqualTo("상품명");
            assertThat(captured.price()).isEqualTo(10_000L);
            assertThat(captured.tags()).containsExactly("tag1");
        }

        @Test
        @DisplayName("registerProduct 메서드에 @PreAuthorize(관리자 또는 판매자 권한) 어노테이션이 부착되어 있다")
        void registerProductHasPreAuthorize() throws NoSuchMethodException {
            Method method = ProductController.class.getMethod("registerProduct", RegisterProductRequest.class);

            PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

            assertThat(preAuthorize).isNotNull();
            assertThat(preAuthorize.value()).contains("ROLE_ADMIN");
            assertThat(preAuthorize.value()).contains("ROLE_SELLER");
            assertThat(preAuthorize.value()).contains("#request.sellerId.toString() == authentication.name");
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products 커서 페이징")
    class GetProductsCursor {

        @Test
        @DisplayName("첫 페이지 조회 시 cursor=-1 파라미터로 UseCase를 호출하고 nextCursor/hasNext를 응답에 포함한다")
        void returnsFirstPageWithCursor() {
            GetProductsResult result = GetProductsResult.from(true, 50L, 100L, List.of());
            when(getProductUseCase.getProducts(
                    eq(1L), eq(List.of(10L, 20L)), eq(-1L), eq(4),
                    eq(Sort.Direction.DESC), eq(ProductSortProperty.ORDER_NUM),
                    eq(ProductSearchTarget.NAME), eq("keyword")
            )).thenReturn(result);

            ResponseEntity<BaseResponse<GetProductsResponse>> response = productController.getProducts(
                    1L, List.of(10L, 20L), -1L, 4,
                    Sort.Direction.DESC, ProductSortProperty.ORDER_NUM,
                    ProductSearchTarget.NAME, "keyword"
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().getProducts().nextCursor()).isEqualTo(50L);
            assertThat(response.getBody().getContent().getProducts().hasNext()).isTrue();
            verify(getProductUseCase).getProducts(
                    eq(1L), eq(List.of(10L, 20L)), eq(-1L), eq(4),
                    eq(Sort.Direction.DESC), eq(ProductSortProperty.ORDER_NUM),
                    eq(ProductSearchTarget.NAME), eq("keyword")
            );
        }

        @Test
        @DisplayName("다음 페이지 조회 시 cursor 값이 UseCase로 전달되고 hasNext=false일 때 더 이상 다음 페이지 없음을 응답한다")
        void returnsNextPageAndNoMoreData() {
            GetProductsResult result = GetProductsResult.from(false, -1L, 100L, List.of());
            when(getProductUseCase.getProducts(
                    eq(1L), eq(List.of()), eq(50L), eq(4),
                    eq(Sort.Direction.DESC), eq(ProductSortProperty.ORDER_NUM),
                    eq(ProductSearchTarget.NAME), eq("")
            )).thenReturn(result);

            ResponseEntity<BaseResponse<GetProductsResponse>> response = productController.getProducts(
                    1L, List.of(), 50L, 4,
                    Sort.Direction.DESC, ProductSortProperty.ORDER_NUM,
                    ProductSearchTarget.NAME, ""
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent().getProducts().hasNext()).isFalse();
            assertThat(response.getBody().getContent().getProducts().nextCursor()).isEqualTo(-1L);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/admin")
    class GetAdminProducts {

        @Test
        @DisplayName("관리자 상품 목록 조회 시 200 OK와 페이지 정보를 반환한다")
        void returnsAdminProducts() {
            GetAdminProductsResult result = GetAdminProductsResult.of(0, 4, 0L, 0, List.of());
            when(getAdminProductsUseCase.getAdminProducts(
                    eq(1L), eq(List.of()), eq(0), eq(4),
                    eq(Sort.Direction.DESC), eq(ProductSortProperty.ORDER_NUM),
                    eq(ProductSearchTarget.NAME), eq("")
            )).thenReturn(result);

            ResponseEntity<BaseResponse<GetAdminProductsResponse>> response = productController.getAdminProducts(
                    1L, List.of(), 0, 4,
                    Sort.Direction.DESC, ProductSortProperty.ORDER_NUM,
                    ProductSearchTarget.NAME, ""
            );

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            verify(getAdminProductsUseCase).getAdminProducts(
                    eq(1L), eq(List.of()), eq(0), eq(4),
                    eq(Sort.Direction.DESC), eq(ProductSortProperty.ORDER_NUM),
                    eq(ProductSearchTarget.NAME), eq("")
            );
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/admin/{id}")
    class GetAdminProductDetail {

        @Test
        @DisplayName("selectedOptionIds가 null이면 빈 리스트로 UseCase에 전달한다")
        void convertsNullSelectedOptionIdsToEmptyList() {
            GetAdminProductDetailResult result = GetAdminProductDetailResult.of(
                    GetProductInfoWithOptionsResult.of(null, List.of()),
                    null, null
            );
            when(getAdminProductDetailUseCase.getAdminProductDetail(eq(1L), eq(List.of()))).thenReturn(result);

            ResponseEntity<BaseResponse<GetAdminProductDetailResponse>> response =
                    productController.getAdminProductDetail(1L, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getAdminProductDetailUseCase).getAdminProductDetail(eq(1L), eq(List.of()));
        }

        @Test
        @DisplayName("selectedOptionIds가 전달되면 그대로 UseCase에 전달한다")
        void passesThroughSelectedOptionIds() {
            GetAdminProductDetailResult result = GetAdminProductDetailResult.of(
                    GetProductInfoWithOptionsResult.of(null, List.of()),
                    null, null
            );
            when(getAdminProductDetailUseCase.getAdminProductDetail(eq(1L), eq(List.of(10L, 20L))))
                    .thenReturn(result);

            ResponseEntity<BaseResponse<GetAdminProductDetailResponse>> response =
                    productController.getAdminProductDetail(1L, List.of(10L, 20L));

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getAdminProductDetailUseCase).getAdminProductDetail(eq(1L), eq(List.of(10L, 20L)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetProductInfo {

        @Test
        @DisplayName("selectedOptionIds가 null이면 빈 리스트로 UseCase에 전달한다")
        void convertsNullSelectedOptionIdsToEmptyList() {
            GetProductInfoWithOptionsResult result = GetProductInfoWithOptionsResult.of(
                    null, List.of(),
                    new GetFilesResult(List.of()), new GetFilesResult(List.of()),
                    List.of()
            );
            when(getProductUseCase.getProductInfo(eq(1L), eq(List.of()))).thenReturn(result);

            ResponseEntity<BaseResponse<GetProductInfoResponse>> response =
                    productController.getProductInfo(1L, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getProductUseCase).getProductInfo(eq(1L), eq(List.of()));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/price-policies/{pricePolicyId}")
    class GetProductInfoByPricePolicyId {

        @Test
        @DisplayName("가격 정책 ID로 상품 정보 조회 시 200 OK를 반환한다")
        void returnsOkByPricePolicyId() {
            GetProductInfoWithOptionsResult result = GetProductInfoWithOptionsResult.of(
                    null, List.of(),
                    new GetFilesResult(List.of()), new GetFilesResult(List.of()),
                    List.of()
            );
            when(getProductUseCase.getProductInfo(eq(500L))).thenReturn(result);

            ResponseEntity<BaseResponse<GetProductInfoResponse>> response =
                    productController.getProductInfoByPricePolicyId(500L);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getProductUseCase).getProductInfo(eq(500L));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products/{id}")
    class UpdateProduct {

        @Test
        @DisplayName("상품 수정 시 200 OK와 수정 결과를 반환하고 userId/isAdmin을 UseCase에 전달한다")
        void returnsOkWithResultAndPassesPrincipal() {
            UpdateProductRequest request = new UpdateProductRequest(
                    "수정상품명", "수정브랜드", "수정상세",
                    true, List.of("tag1"), null
            );
            UUID productKey = UUID.randomUUID();
            UpdateProductResult result = new UpdateProductResult(1L, productKey);
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("100");
            when(updateProductUseCase.update(eq(100L), anyBoolean(), any(UpdateProductCommand.class)))
                    .thenReturn(result);

            ResponseEntity<BaseResponse<UpdateProductResponse>> response =
                    productController.updateProduct(1L, request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent().id()).isEqualTo(1L);
            assertThat(response.getBody().getContent().productKey()).isEqualTo(productKey);

            ArgumentCaptor<UpdateProductCommand> captor = ArgumentCaptor.forClass(UpdateProductCommand.class);
            verify(updateProductUseCase).update(eq(100L), anyBoolean(), captor.capture());
            UpdateProductCommand captured = captor.getValue();
            assertThat(captured.id()).isEqualTo(1L);
            assertThat(captured.name()).isEqualTo("수정상품명");
        }

        @Test
        @DisplayName("updateProduct 메서드에 @PreAuthorize(관리자 또는 판매자) 어노테이션이 부착되어 있다")
        void updateProductHasPreAuthorize() throws NoSuchMethodException {
            Method method = ProductController.class.getMethod(
                    "updateProduct",
                    Long.class, UpdateProductRequest.class, OAuth2AuthenticatedPrincipal.class
            );

            PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

            assertThat(preAuthorize).isNotNull();
            assertThat(preAuthorize.value()).contains("ROLE_ADMIN");
            assertThat(preAuthorize.value()).contains("ROLE_SELLER");
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/products/{id}/tags/order")
    class ReorderProductTags {

        @Test
        @DisplayName("태그 순서 변경 시 200 OK를 반환하고 매핑된 커맨드를 UseCase에 전달한다")
        void reordersTags() {
            ReorderProductTagsRequest request = new ReorderProductTagsRequest(
                    List.of(
                            new ReorderProductTagsRequest.TagOrderItem(1L, 1L),
                            new ReorderProductTagsRequest.TagOrderItem(2L, 2L)
                    )
            );
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("100");

            ResponseEntity<BaseResponse<Void>> response =
                    productController.reorderProductTags(1L, request, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(reorderProductTagsUseCase).reorderProductTags(
                    eq(100L), anyBoolean(), any(ReorderProductTagsCommand.class)
            );
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{id} 본인 상품 검증 및 이미지 정리 전달")
    class DeleteProductOwnership {

        @Test
        @DisplayName("상품 삭제 시 DeleteProductCommand에 대상 상품 ID를 담아 UseCase에 전달한다")
        void passesProductIdInCommand() {
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("100");

            ResponseEntity<BaseResponse<Void>> response = productController.deleteProduct(99L, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<DeleteProductCommand> captor = ArgumentCaptor.forClass(DeleteProductCommand.class);
            verify(deleteProductUseCase).delete(eq(100L), anyBoolean(), captor.capture());
            assertThat(captor.getValue().id()).isEqualTo(99L);
        }

        @Test
        @DisplayName("deleteProduct 메서드에 @PreAuthorize(관리자 또는 판매자) 어노테이션이 부착되어 있다")
        void deleteProductHasPreAuthorize() throws NoSuchMethodException {
            Method method = ProductController.class.getMethod(
                    "deleteProduct", Long.class, OAuth2AuthenticatedPrincipal.class
            );

            PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

            assertThat(preAuthorize).isNotNull();
            assertThat(preAuthorize.value()).contains("ROLE_ADMIN");
            assertThat(preAuthorize.value()).contains("ROLE_SELLER");
        }

        @Test
        @DisplayName("상품 이미지 삭제 시 DeleteProductImageCommand에 productId/fileId를 담아 UseCase에 전달한다")
        void passesProductIdAndFileIdToImageDelete() {
            OAuth2AuthenticatedPrincipal principal = mock(OAuth2AuthenticatedPrincipal.class);
            when(principal.getName()).thenReturn("100");

            ResponseEntity<BaseResponse<Void>> response =
                    productController.deleteProductImage(99L, 77L, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            ArgumentCaptor<DeleteProductImageCommand> captor
                    = ArgumentCaptor.forClass(DeleteProductImageCommand.class);
            verify(deleteProductImageUseCase).delete(eq(100L), anyBoolean(), captor.capture());
            assertThat(captor.getValue().id()).isEqualTo(99L);
            assertThat(captor.getValue().fileId()).isEqualTo(77L);
        }
    }

    @Nested
    @DisplayName("RateLimited 어노테이션 부착 검증")
    class RateLimitedAnnotation {

        @Test
        @DisplayName("getProducts 메서드에 @RateLimited(key=products-cursor, maxRequests=60, windowSeconds=60)이 부착되어 있다")
        void getProductsHasRateLimited() throws NoSuchMethodException {
            Method method = ProductController.class.getMethod(
                    "getProducts",
                    Long.class, List.class, Long.class, int.class,
                    Sort.Direction.class, ProductSortProperty.class,
                    ProductSearchTarget.class, String.class
            );

            RateLimited rateLimited = method.getAnnotation(RateLimited.class);

            assertThat(rateLimited).isNotNull();
            assertThat(rateLimited.key()).isEqualTo("products-cursor");
            assertThat(rateLimited.maxRequests()).isEqualTo(60);
            assertThat(rateLimited.windowSeconds()).isEqualTo(60);
        }
    }
}
