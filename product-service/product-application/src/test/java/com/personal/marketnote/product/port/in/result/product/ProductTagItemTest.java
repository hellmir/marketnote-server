package com.personal.marketnote.product.port.in.result.product;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.product.domain.product.ProductTag;
import com.personal.marketnote.product.domain.product.ProductTagSnapshotState;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTagItemTest {

    @Test
    @DisplayName("ProductTag로부터 ProductTagItem을 생성한다 - id/name/orderNum 매핑")
    void shouldCreateProductTagItemFromProductTagMappingIdNameOrderNum() {
        // given
        ProductTag tag = createTag(10L, "NEW", 1L);

        // when
        ProductTagItem item = ProductTagItem.from(tag);

        // then
        assertThat(item.id()).isEqualTo(10L);
        assertThat(item.name()).isEqualTo("NEW");
        assertThat(item.orderNum()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ProductTagItem record는 productId와 status 필드를 노출하지 않는다")
    void shouldNotExposeProductIdAndStatusInRecordComponents() {
        // given
        RecordComponent[] components = ProductTagItem.class.getRecordComponents();

        // when
        Set<String> componentNames = Arrays.stream(components)
                .map(RecordComponent::getName)
                .collect(Collectors.toSet());

        // then
        assertThat(components).hasSize(3);
        assertThat(componentNames).containsExactlyInAnyOrder("id", "name", "orderNum");
        assertThat(componentNames).doesNotContain("productId", "status");
    }

    @Test
    @DisplayName("ProductTagItem을 여러 태그로부터 일괄 생성한다")
    void shouldCreateProductTagItemsFromMultipleTags() {
        // given
        List<ProductTag> tags = List.of(
                createTag(1L, "NEW", 1L),
                createTag(2L, "HOT", 2L),
                createTag(3L, "BEST", 3L)
        );

        // when
        List<ProductTagItem> items = tags.stream()
                .map(ProductTagItem::from)
                .toList();

        // then
        assertThat(items).hasSize(3);
        assertThat(items)
                .extracting(ProductTagItem::id, ProductTagItem::name, ProductTagItem::orderNum)
                .containsExactly(
                        Tuple.tuple(1L, "NEW", 1L),
                        Tuple.tuple(2L, "HOT", 2L),
                        Tuple.tuple(3L, "BEST", 3L)
                );
    }

    private ProductTag createTag(Long id, String name, Long orderNum) {
        return ProductTag.from(
                ProductTagSnapshotState.builder()
                        .id(id)
                        .productId(100L)
                        .name(name)
                        .orderNum(orderNum)
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
