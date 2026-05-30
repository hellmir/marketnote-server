package com.personal.marketnote.community.domain.post;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostFilterCategoryTest {

    @Test
    @DisplayName("IS_ANSWERED 필터 카테고리가 존재하며 설명은 '답변 완료 여부'이다")
    void isAnswered_existsWithDescription() {
        assertThat(PostFilterCategory.IS_ANSWERED).isNotNull();
        assertThat(PostFilterCategory.IS_ANSWERED.getDescription()).isEqualTo("답변 완료 여부");
    }

    @Test
    @DisplayName("IS_ANSWERED 필터에서 filterValue가 TRUE이면 isAnsweredOnly가 true를 반환한다")
    void isAnsweredOnly_trueValue_returnsTrue() {
        boolean result = PostFilterCategory.IS_ANSWERED.isAnsweredOnly(PostFilterValue.TRUE);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("IS_ANSWERED 필터에서 filterValue가 TRUE가 아니면 isAnsweredOnly가 false를 반환한다")
    void isAnsweredOnly_nonTrueValue_returnsFalse() {
        boolean result = PostFilterCategory.IS_ANSWERED.isAnsweredOnly(PostFilterValue.ORDER_PAYMENT);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("IS_ANSWERED 필터는 isPublicOnly에서 false를 반환한다")
    void isAnswered_isPublicOnly_returnsFalse() {
        boolean result = PostFilterCategory.IS_ANSWERED.isPublicOnly(PostFilterValue.TRUE);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("IS_ANSWERED 필터는 isMineFiltered에서 false를 반환한다")
    void isAnswered_isMineFiltered_returnsFalse() {
        boolean result = PostFilterCategory.IS_ANSWERED.isMineFiltered(PostFilterValue.TRUE);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("IS_PUBLIC 필터는 isAnsweredOnly에서 false를 반환한다")
    void isPublic_isAnsweredOnly_returnsFalse() {
        boolean result = PostFilterCategory.IS_PUBLIC.isAnsweredOnly(PostFilterValue.TRUE);

        assertThat(result).isFalse();
    }
}
