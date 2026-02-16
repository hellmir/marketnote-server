package com.personal.marketnote.product.service.option;

import com.personal.marketnote.common.adapter.out.persistence.audit.EntityStatus;
import com.personal.marketnote.product.domain.option.ProductOption;
import com.personal.marketnote.product.domain.option.ProductOptionCategory;
import com.personal.marketnote.product.domain.option.ProductOptionCategorySnapshotState;
import com.personal.marketnote.product.domain.option.ProductOptionSnapshotState;
import com.personal.marketnote.product.port.in.result.option.GetProductOptionsResult;
import com.personal.marketnote.product.port.in.result.option.ProductOptionCategoryItemResult;
import com.personal.marketnote.product.port.in.result.option.ProductOptionItemResult;
import com.personal.marketnote.product.port.out.productoption.FindProductOptionCategoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductOptionsUseCaseTest {
    @Mock
    private FindProductOptionCategoryPort findProductOptionCategoryPort;

    @InjectMocks
    private GetProductOptionsService getProductOptionsService;

    @Test
    @DisplayName("상품 옵션 목록 조회 시 카테고리와 하위 옵션을 매핑해 반환한다")
    void getProductOptions_mapsCategoriesAndOptions() {
        Long productId = 10L;
        ProductOptionCategory category1 = buildCategory(
                100L,
                "색상",
                1L,
                EntityStatus.ACTIVE,
                List.of(
                        buildOption(1001L, "RED", EntityStatus.ACTIVE),
                        buildOption(1002L, "BLUE", EntityStatus.ACTIVE)
                )
        );
        ProductOptionCategory category2 = buildCategory(
                200L,
                "사이즈",
                2L,
                EntityStatus.UNEXPOSED,
                List.of(buildOption(2001L, "M", EntityStatus.INACTIVE))
        );

        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(productId))
                .thenReturn(List.of(category1, category2));

        GetProductOptionsResult result = getProductOptionsService.getProductOptions(productId);

        assertThat(result.categories()).hasSize(2);

        ProductOptionCategoryItemResult item1 = result.categories().getFirst();
        assertThat(item1.id()).isEqualTo(100L);
        assertThat(item1.name()).isEqualTo("색상");
        assertThat(item1.orderNum()).isEqualTo(1L);
        assertThat(item1.status()).isEqualTo(EntityStatus.ACTIVE.name());
        assertThat(item1.options()).extracting(ProductOptionItemResult::id)
                .containsExactly(1001L, 1002L);
        assertThat(item1.options()).extracting(ProductOptionItemResult::content)
                .containsExactly("RED", "BLUE");
        assertThat(item1.options()).extracting(ProductOptionItemResult::status)
                .containsExactly(EntityStatus.ACTIVE.name(), EntityStatus.ACTIVE.name());

        ProductOptionCategoryItemResult item2 = result.categories().getLast();
        assertThat(item2.id()).isEqualTo(200L);
        assertThat(item2.name()).isEqualTo("사이즈");
        assertThat(item2.orderNum()).isEqualTo(2L);
        assertThat(item2.status()).isEqualTo(EntityStatus.UNEXPOSED.name());
        assertThat(item2.options()).extracting(ProductOptionItemResult::id)
                .containsExactly(2001L);
        assertThat(item2.options()).extracting(ProductOptionItemResult::content)
                .containsExactly("M");
        assertThat(item2.options()).extracting(ProductOptionItemResult::status)
                .containsExactly(EntityStatus.INACTIVE.name());
    }

    @Test
    @DisplayName("상품 옵션 목록 조회 시 카테고리가 없으면 빈 목록을 반환한다")
    void getProductOptions_emptyCategories_returnsEmpty() {
        Long productId = 11L;

        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(productId))
                .thenReturn(List.of());

        GetProductOptionsResult result = getProductOptionsService.getProductOptions(productId);

        assertThat(result.categories()).isEmpty();
        verify(findProductOptionCategoryPort).findActiveWithOptionsByProductId(productId);
    }

    @Test
    @DisplayName("상품 옵션 목록 조회 시 포트 예외를 전파한다")
    void getProductOptions_portFails_propagates() {
        Long productId = 12L;
        RuntimeException exception = new RuntimeException("find fail");

        when(findProductOptionCategoryPort.findActiveWithOptionsByProductId(productId))
                .thenThrow(exception);

        assertThatThrownBy(() -> getProductOptionsService.getProductOptions(productId))
                .isSameAs(exception);
    }

    private ProductOptionCategory buildCategory(
            Long id,
            String name,
            Long orderNum,
            EntityStatus status,
            List<ProductOption> options
    ) {
        return ProductOptionCategory.from(
                ProductOptionCategorySnapshotState.builder()
                        .id(id)
                        .name(name)
                        .orderNum(orderNum)
                        .status(status)
                        .options(options)
                        .build()
        );
    }

    private ProductOption buildOption(Long id, String content, EntityStatus status) {
        return ProductOption.from(
                ProductOptionSnapshotState.builder()
                        .id(id)
                        .content(content)
                        .status(status)
                        .build()
        );
    }
}
