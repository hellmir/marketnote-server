package com.personal.marketnote.product.service.product;

import com.personal.marketnote.product.domain.product.ProductSearchTarget;
import com.personal.marketnote.product.port.in.result.product.GetProductSearchTargetsResult;
import com.personal.marketnote.product.port.in.result.product.ProductSearchTargetItemResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GetProductSearchTargetsUseCaseTest {
    private final GetProductSearchTargetsService service = new GetProductSearchTargetsService();

    @Test
    @DisplayName("상품 검색 대상 목록 조회 시 모든 검색 대상을 반환한다")
    void getProductSearchTargets_returnsAll() {
        GetProductSearchTargetsResult result = service.getProductSearchTargets();

        assertThat(result.targets()).hasSize(ProductSearchTarget.values().length);
    }

    @Test
    @DisplayName("상품 검색 대상 목록 조회 시 이름과 설명이 매핑된다")
    void getProductSearchTargets_mapsNameAndDescription() {
        GetProductSearchTargetsResult result = service.getProductSearchTargets();

        ProductSearchTarget[] targets = ProductSearchTarget.values();
        ProductSearchTargetItemResult[] items = result.targets();

        assertThat(items).hasSize(targets.length);
        for (int index = 0; index < targets.length; index += 1) {
            assertThat(items[index].name()).isEqualTo(targets[index].name());
            assertThat(items[index].description()).isEqualTo(targets[index].getDescription());
        }
    }
}
