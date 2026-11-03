package com.personal.marketnote.community.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class FaqPostCategoryTest {

    @Test
    @DisplayName("ORDER_DELIVERY는 default 카테고리이다")
    void orderDelivery_isDefaultCategory() {
        assertThat(FaqPostCategory.ORDER_DELIVERY.isDefault()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = FaqPostCategory.class, mode = EnumSource.Mode.EXCLUDE, names = {"ORDER_DELIVERY"})
    @DisplayName("ORDER_DELIVERY를 제외한 FaqPostCategory는 default 카테고리가 아니다")
    void nonOrderDelivery_isNotDefaultCategory(FaqPostCategory category) {
        assertThat(category.isDefault()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(NoticePostCategory.class)
    @DisplayName("NoticePostCategory의 모든 값은 default 카테고리가 아니다")
    void noticePostCategory_isNotDefaultCategory(NoticePostCategory category) {
        assertThat(category.isDefault()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(ProductInqueryPostCategory.class)
    @DisplayName("ProductInqueryPostCategory의 모든 값은 default 카테고리가 아니다")
    void productInqueryPostCategory_isNotDefaultCategory(ProductInqueryPostCategory category) {
        assertThat(category.isDefault()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(OneOnOneInqueryPostCategory.class)
    @DisplayName("OneOnOneInqueryPostCategory의 모든 값은 default 카테고리가 아니다")
    void oneOnOneInqueryPostCategory_isNotDefaultCategory(OneOnOneInqueryPostCategory category) {
        assertThat(category.isDefault()).isFalse();
    }
}
