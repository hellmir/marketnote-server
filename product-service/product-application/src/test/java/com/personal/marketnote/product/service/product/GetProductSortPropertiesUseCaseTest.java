package com.personal.marketnote.product.service.product;

import com.personal.marketnote.product.domain.product.ProductSortProperty;
import com.personal.marketnote.product.port.in.result.product.GetProductSortPropertiesResult;
import com.personal.marketnote.product.port.in.result.product.ProductSortPropertyItemResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GetProductSortPropertiesUseCaseTest {
    private final GetProductSortPropertiesService service = new GetProductSortPropertiesService();

    @Test
    @DisplayName("상품 정렬 속성 목록 조회 시 모든 정렬 속성을 반환한다")
    void getProductSortProperties_returnsAll() {
        GetProductSortPropertiesResult result = service.getProductSortProperties();

        assertThat(result.properties()).hasSize(ProductSortProperty.values().length);
    }

    @Test
    @DisplayName("상품 정렬 속성 목록 조회 시 이름과 설명이 매핑된다")
    void getProductSortProperties_mapsNameAndDescription() {
        GetProductSortPropertiesResult result = service.getProductSortProperties();

        ProductSortProperty[] properties = ProductSortProperty.values();
        ProductSortPropertyItemResult[] items = result.properties();

        assertThat(items).hasSize(properties.length);
        for (int index = 0; index < properties.length; index += 1) {
            assertThat(items[index].name()).isEqualTo(properties[index].name());
            assertThat(items[index].description()).isEqualTo(properties[index].getDescription());
        }
    }
}
