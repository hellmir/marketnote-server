package com.personal.marketnote.product.domain.product;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Test
    @DisplayName("CreateState로 생성하면 태그가 ProductTag로 변환된다")
    void shouldConvertTagsToProductTagsWhenCreatedFromCreateState() {
        ProductCreateState state = ProductCreateState.builder()
                .sellerId(1L)
                .name("테스트 상품")
                .brandName("테스트 브랜드")
                .detail("상품 설명")
                .findAllOptionsYn(false)
                .tags(List.of(
                        ProductTagCreateState.builder().productId(null).name("태그1").build(),
                        ProductTagCreateState.builder().productId(null).name("태그2").build()
                ))
                .build();

        Product product = Product.from(state);

        assertThat(product.getProductTags()).hasSize(2);
        assertThat(product.getProductTags().get(0).getName()).isEqualTo("태그1");
        assertThat(product.getProductTags().get(1).getName()).isEqualTo("태그2");
        assertThat(product.getProductTags().get(0).getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("CreateState에 태그가 null이면 빈 리스트로 설정된다")
    void shouldSetEmptyTagsWhenCreateStateTagsIsNull() {
        ProductCreateState state = ProductCreateState.builder()
                .sellerId(1L)
                .name("테스트 상품")
                .brandName("테스트 브랜드")
                .detail("상품 설명")
                .findAllOptionsYn(false)
                .tags(null)
                .build();

        Product product = Product.from(state);

        assertThat(product.getProductTags()).isEmpty();
    }

    @Test
    @DisplayName("CreateState로 생성하면 status가 기본값 ACTIVE로 설정된다")
    void shouldSetDefaultStatusToActiveWhenCreatedFromCreateState() {
        ProductCreateState state = createDefaultCreateState();

        Product product = Product.from(state);

        assertThat(product.isActive()).isTrue();
        assertThat(product.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("SnapshotState로 복원하면 모든 필드가 올바르게 매핑된다")
    void shouldRestoreAllFieldsFromSnapshotState() {
        List<ProductTag> tags = List.of(
                ProductTag.from(ProductTagSnapshotState.builder()
                        .id(10L).productId(1L).name("태그A").orderNum(1L).status(EntityStatus.ACTIVE).build())
        );
        ProductSnapshotState state = ProductSnapshotState.builder()
                .id(1L)
                .sellerId(100L)
                .name("테스트 상품")
                .brandName("테스트 브랜드")
                .detail("상품 상세 설명")
                .sales(50)
                .viewCount(1000L)
                .popularity(500L)
                .findAllOptionsYn(true)
                .productTags(tags)
                .orderNum(1L)
                .status(EntityStatus.ACTIVE)
                .build();

        Product product = Product.from(state);

        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getSellerId()).isEqualTo(100L);
        assertThat(product.getName()).isEqualTo("테스트 상품");
        assertThat(product.getBrandName()).isEqualTo("테스트 브랜드");
        assertThat(product.getDetail()).isEqualTo("상품 상세 설명");
        assertThat(product.getSales()).isEqualTo(50);
        assertThat(product.getViewCount()).isEqualTo(1000L);
        assertThat(product.getPopularity()).isEqualTo(500L);
        assertThat(product.isFindAllOptionsYn()).isTrue();
        assertThat(product.getProductTags()).hasSize(1);
        assertThat(product.getOrderNum()).isEqualTo(1L);
        assertThat(product.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("SnapshotState로 복원하면 status가 SnapshotState의 값으로 설정된다")
    void shouldRestoreStatusFromSnapshotState() {
        ProductSnapshotState state = ProductSnapshotState.builder()
                .id(1L)
                .sellerId(100L)
                .name("테스트 상품")
                .brandName("테스트 브랜드")
                .detail("상품 설명")
                .findAllOptionsYn(false)
                .productTags(List.of())
                .status(EntityStatus.INACTIVE)
                .build();

        Product product = Product.from(state);

        assertThat(product.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        assertThat(product.isInactive()).isTrue();
    }

    @Test
    @DisplayName("update를 호출하면 name, brandName, detail, findAllOptionsYn, productTags가 변경된다")
    void shouldUpdateFieldsAndTags() {
        Product product = createActiveProduct();

        product.update("수정된 상품명", "수정된 브랜드", "수정된 설명", true, List.of("새태그1", "새태그2", "새태그3"));

        assertThat(product.getName()).isEqualTo("수정된 상품명");
        assertThat(product.getBrandName()).isEqualTo("수정된 브랜드");
        assertThat(product.getDetail()).isEqualTo("수정된 설명");
        assertThat(product.isFindAllOptionsYn()).isTrue();
        assertThat(product.getProductTags()).hasSize(3);
        assertThat(product.getProductTags().get(0).getName()).isEqualTo("새태그1");
        assertThat(product.getProductTags().get(1).getName()).isEqualTo("새태그2");
        assertThat(product.getProductTags().get(2).getName()).isEqualTo("새태그3");
    }

    @Test
    @DisplayName("delete를 호출하면 상태가 INACTIVE로 변경된다")
    void shouldChangeStatusToInactiveWhenDeleted() {
        Product product = createActiveProduct();

        product.delete();

        assertThat(product.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        assertThat(product.isInactive()).isTrue();
        assertThat(product.isActive()).isFalse();
    }

    @Test
    @DisplayName("isActive는 ACTIVE 상태에서 true를 반환한다")
    void shouldReturnTrueWhenStatusIsActive() {
        Product product = createActiveProduct();

        assertThat(product.isActive()).isTrue();
        assertThat(product.isInactive()).isFalse();
    }

    @Test
    @DisplayName("isInactive는 INACTIVE 상태에서 true를 반환한다")
    void shouldReturnTrueWhenStatusIsInactive() {
        Product product = createActiveProduct();
        product.delete();

        assertThat(product.isInactive()).isTrue();
        assertThat(product.isActive()).isFalse();
    }

    @Test
    @DisplayName("updateStatus에 false를 전달하면 INACTIVE로 변경된다")
    void shouldChangeToInactiveWhenUpdateStatusWithFalse() {
        Product product = createActiveProduct();

        product.updateStatus(false);

        assertThat(product.isInactive()).isTrue();
    }

    @Test
    @DisplayName("updateStatus에 true를 전달하면 ACTIVE로 변경된다")
    void shouldChangeToActiveWhenUpdateStatusWithTrue() {
        Product product = createActiveProduct();
        product.delete();

        product.updateStatus(true);

        assertThat(product.isActive()).isTrue();
    }

    @Test
    @DisplayName("CreateState로 생성하면 id, sales, viewCount, popularity, orderNum이 null이다")
    void shouldSetNullForUnspecifiedFieldsWhenCreatedFromCreateState() {
        ProductCreateState state = createDefaultCreateState();

        Product product = Product.from(state);

        assertThat(product.getId()).isNull();
        assertThat(product.getSales()).isNull();
        assertThat(product.getViewCount()).isNull();
        assertThat(product.getPopularity()).isNull();
        assertThat(product.getOrderNum()).isNull();
        assertThat(product.getDefaultPricePolicy()).isNull();
    }

    @Test
    @DisplayName("update 호출 시 tags가 null이면 NullPointerException이 발생한다")
    void shouldThrowNpeWhenUpdateTagsIsNull() {
        Product product = createActiveProduct();

        assertThatThrownBy(() -> product.update("이름", "브랜드", "설명", false, null))
                .isInstanceOf(NullPointerException.class);
    }

    private ProductCreateState createDefaultCreateState() {
        return ProductCreateState.builder()
                .sellerId(1L)
                .name("테스트 상품")
                .brandName("테스트 브랜드")
                .detail("상품 설명")
                .findAllOptionsYn(false)
                .tags(List.of(
                        ProductTagCreateState.builder().productId(null).name("태그1").build()
                ))
                .build();
    }

    private Product createActiveProduct() {
        return Product.from(ProductSnapshotState.builder()
                .id(1L)
                .sellerId(100L)
                .name("테스트 상품")
                .brandName("테스트 브랜드")
                .detail("상품 설명")
                .findAllOptionsYn(false)
                .productTags(List.of())
                .orderNum(1L)
                .status(EntityStatus.ACTIVE)
                .build());
    }
}
