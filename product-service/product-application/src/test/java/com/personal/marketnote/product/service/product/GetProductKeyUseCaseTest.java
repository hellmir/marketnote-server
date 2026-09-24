package com.personal.marketnote.product.service.product;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.product.Product;
import com.personal.marketnote.product.domain.product.ProductSnapshotState;
import com.personal.marketnote.product.exception.NotProductOwnerException;
import com.personal.marketnote.product.exception.ProductNotFoundException;
import com.personal.marketnote.product.port.in.result.product.GetProductKeyResult;
import com.personal.marketnote.product.port.in.usecase.product.GetProductUseCase;
import com.personal.marketnote.product.port.out.product.FindProductPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductKeyUseCaseTest {

    private static final UUID TEST_PRODUCT_KEY = UUID.fromString("018f0000-0000-7000-8000-000000000001");

    @Mock
    private GetProductUseCase getProductUseCase;
    @Mock
    private FindProductPort findProductPort;

    @InjectMocks
    private GetProductKeyService getProductKeyService;

    @Test
    @DisplayName("상품 productKey 조회 시 판매자가 본인 상품이 아니면 NotProductOwnerException을 던진다")
    void getProductKey_notOwner_throws() {
        Long productId = 10L;
        Long userId = 1L;

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(false);

        assertThatThrownBy(() -> getProductKeyService.getProductKey(productId, userId, false))
                .isInstanceOf(NotProductOwnerException.class)
                .hasMessageContaining("관리자 또는 상품 판매자가 아닙니다");

        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verifyNoInteractions(getProductUseCase);
    }

    @Test
    @DisplayName("상품 productKey 조회 시 관리자는 소유권 검증 없이 productKey를 반환한다")
    void getProductKey_adminSkipsOwnerCheck_returnsKey() {
        Long productId = 11L;
        Product product = buildProduct(productId, TEST_PRODUCT_KEY);

        when(getProductUseCase.getProduct(productId)).thenReturn(product);

        GetProductKeyResult result = getProductKeyService.getProductKey(productId, 99L, true);

        assertThat(result.productKey()).isEqualTo(TEST_PRODUCT_KEY);
        verify(getProductUseCase).getProduct(productId);
        verifyNoInteractions(findProductPort);
    }

    @Test
    @DisplayName("상품 productKey 조회 시 본인 상품을 판매하는 판매자이면 productKey를 반환한다")
    void getProductKey_owner_returnsKey() {
        Long productId = 20L;
        Long userId = 2L;
        Product product = buildProduct(productId, TEST_PRODUCT_KEY);

        when(findProductPort.existsByIdAndSellerId(productId, userId)).thenReturn(true);
        when(getProductUseCase.getProduct(productId)).thenReturn(product);

        GetProductKeyResult result = getProductKeyService.getProductKey(productId, userId, false);

        assertThat(result.productKey()).isEqualTo(TEST_PRODUCT_KEY);
        verify(findProductPort).existsByIdAndSellerId(productId, userId);
        verify(getProductUseCase).getProduct(productId);
    }

    @Test
    @DisplayName("상품 productKey 조회 시 상품이 존재하지 않으면 ProductNotFoundException을 전파한다")
    void getProductKey_productNotFound_propagates() {
        Long productId = 30L;
        ProductNotFoundException exception = new ProductNotFoundException(productId);

        when(getProductUseCase.getProduct(productId)).thenThrow(exception);

        assertThatThrownBy(() -> getProductKeyService.getProductKey(productId, 99L, true))
                .isSameAs(exception);

        verify(getProductUseCase).getProduct(productId);
        verifyNoInteractions(findProductPort);
    }

    private Product buildProduct(Long id, UUID productKey) {
        return Product.from(
                ProductSnapshotState.builder()
                        .id(id)
                        .productKey(productKey)
                        .sellerId(1L)
                        .name("상품-" + id)
                        .brandName("브랜드-" + id)
                        .detail("설명-" + id)
                        .findAllOptionsYn(false)
                        .productTags(List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
