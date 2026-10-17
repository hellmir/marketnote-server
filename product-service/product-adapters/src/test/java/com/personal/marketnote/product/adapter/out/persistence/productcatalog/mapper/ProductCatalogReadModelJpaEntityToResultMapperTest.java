package com.personal.marketnote.product.adapter.out.persistence.productcatalog.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.product.adapter.out.persistence.productcatalog.entity.ProductCatalogReadModelJpaEntity;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogItemResult;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogOptionSnapshot;
import com.personal.marketnote.product.port.in.result.product.ProductCatalogTagSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCatalogReadModelJpaEntityToResultMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("product_tags/selected_options JSON 을 역직렬화하여 스냅샷 목록으로 복원한다")
    void parsesProductTagsAndSelectedOptionsFromJson() {
        ProductCatalogReadModelJpaEntity entity = buildEntity(
                "[{\"id\":10,\"name\":\"NEW\",\"orderNum\":1},{\"id\":11,\"name\":\"HOT\",\"orderNum\":2}]",
                "[{\"id\":100,\"content\":\"색상: 빨강\"},{\"id\":101,\"content\":\"사이즈: L\"}]",
                150L
        );

        ProductCatalogItemResult result =
                ProductCatalogReadModelJpaEntityToResultMapper.mapToResult(entity, objectMapper);

        assertThat(result.sales()).isEqualTo(150L);
        assertThat(result.productTags())
                .extracting(ProductCatalogTagSnapshot::id, ProductCatalogTagSnapshot::name,
                        ProductCatalogTagSnapshot::orderNum)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(10L, "NEW", 1L),
                        org.assertj.core.groups.Tuple.tuple(11L, "HOT", 2L)
                );
        assertThat(result.selectedOptions())
                .extracting(ProductCatalogOptionSnapshot::id, ProductCatalogOptionSnapshot::content)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(100L, "색상: 빨강"),
                        org.assertj.core.groups.Tuple.tuple(101L, "사이즈: L")
                );
    }

    @Test
    @DisplayName("product_tags/selected_options 가 null 이면 빈 리스트를 반환한다")
    void returnsEmptyListWhenJsonNull() {
        ProductCatalogReadModelJpaEntity entity = buildEntity(null, null, 0L);

        ProductCatalogItemResult result =
                ProductCatalogReadModelJpaEntityToResultMapper.mapToResult(entity, objectMapper);

        assertThat(result.productTags()).isEmpty();
        assertThat(result.selectedOptions()).isEmpty();
    }

    @Test
    @DisplayName("product_tags JSON 역직렬화 실패 시 빈 리스트를 반환하고 다른 필드는 보존한다")
    void returnsEmptyListWhenJsonInvalid() {
        ProductCatalogReadModelJpaEntity entity = buildEntity("invalid-json", "also-invalid", 5L);

        ProductCatalogItemResult result =
                ProductCatalogReadModelJpaEntityToResultMapper.mapToResult(entity, objectMapper);

        assertThat(result.productTags()).isEmpty();
        assertThat(result.selectedOptions()).isEmpty();
        assertThat(result.sales()).isEqualTo(5L);
    }

    private ProductCatalogReadModelJpaEntity buildEntity(
            String productTagsJson, String selectedOptionsJson, Long sales
    ) {
        ProductCatalogReadModelJpaEntity entity = ProductCatalogReadModelJpaEntity.of(
                1L, 100L, 200L, null,
                "상품", "브랜드",
                10000L, 9000L, BigDecimal.valueOf(10.0),
                100L, BigDecimal.ZERO,
                0L, 0L,
                sales, productTagsJson, selectedOptionsJson
        );
        ReflectionTestUtils.setField(entity, "status", com.personal.marketnote.common.domain.EntityStatus.ACTIVE);
        return entity;
    }
}
