package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.community.domain.post.exception.InvalidPostCategoryForBoardException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoardTest {

    @Test
    @DisplayName("NOTICE 게시판은 NoticePostCategory를 반환한다")
    void shouldResolveCategoryForNoticeBoard() {
        PostCategory result = Board.NOTICE.resolveCategory("ANNOUNCEMENT");

        assertThat(result).isEqualTo(NoticePostCategory.ANNOUNCEMENT);
    }

    @Test
    @DisplayName("FAQ 게시판은 FaqPostCategory를 반환한다")
    void shouldResolveCategoryForFaqBoard() {
        PostCategory result = Board.FAQ.resolveCategory("ORDER_PAYMENT");

        assertThat(result).isEqualTo(FaqPostCategory.ORDER_PAYMENT);
    }

    @Test
    @DisplayName("PRODUCT_INQUERY 게시판은 ProductInqueryPostCategory를 반환한다")
    void shouldResolveCategoryForProductInqueryBoard() {
        PostCategory result = Board.PRODUCT_INQUERY.resolveCategory("PRODUCT_QUESTION");

        assertThat(result).isEqualTo(ProductInqueryPostCategory.PRODUCT_QUESTION);
    }

    @Test
    @DisplayName("ONE_ON_ONE_INQUERY 게시판은 OneOnOneInqueryPostCategory를 반환한다")
    void shouldResolveCategoryForOneOnOneInqueryBoard() {
        PostCategory result = Board.ONE_ON_ONE_INQUERY.resolveCategory("ORDER_PAYMENT");

        assertThat(result).isEqualTo(OneOnOneInqueryPostCategory.ORDER_PAYMENT);
    }

    @Test
    @DisplayName("잘못된 카테고리 코드이면 InvalidPostCategoryForBoardException이 발생한다")
    void shouldThrowExceptionWhenCategoryCodeIsInvalid() {
        assertThatThrownBy(() -> Board.NOTICE.resolveCategory("INVALID_CODE"))
                .isInstanceOf(InvalidPostCategoryForBoardException.class);
    }

    @Test
    @DisplayName("NOTICE와 FAQ는 관리자 권한이 필요하다")
    void shouldRequireAdminForNoticeAndFaq() {
        assertThat(Board.NOTICE.isAdminRequired()).isTrue();
        assertThat(Board.FAQ.isAdminRequired()).isTrue();
        assertThat(Board.PRODUCT_INQUERY.isAdminRequired()).isFalse();
        assertThat(Board.ONE_ON_ONE_INQUERY.isAdminRequired()).isFalse();
    }

    @Test
    @DisplayName("NOTICE와 FAQ는 비회원 열람 가능 게시판이다")
    void shouldAllowNonMemberViewForNoticeAndFaq() {
        assertThat(Board.NOTICE.isNonMemberViewBoard()).isTrue();
        assertThat(Board.FAQ.isNonMemberViewBoard()).isTrue();
        assertThat(Board.PRODUCT_INQUERY.isNonMemberViewBoard()).isFalse();
        assertThat(Board.ONE_ON_ONE_INQUERY.isNonMemberViewBoard()).isFalse();
    }

    @Test
    @DisplayName("NOTICE와 FAQ는 수정 가능 게시판이다")
    void shouldBeEditableForNoticeAndFaq() {
        assertThat(Board.NOTICE.isEditable()).isTrue();
        assertThat(Board.FAQ.isEditable()).isTrue();
        assertThat(Board.PRODUCT_INQUERY.isEditable()).isFalse();
        assertThat(Board.ONE_ON_ONE_INQUERY.isEditable()).isFalse();
    }

    @Test
    @DisplayName("ONE_ON_ONE_INQUERY를 제외한 게시판은 작성자 마스킹이 필요하다")
    void shouldRequireWriterMaskingExceptOneOnOneInquery() {
        assertThat(Board.NOTICE.requiresWriterMasking()).isTrue();
        assertThat(Board.FAQ.requiresWriterMasking()).isTrue();
        assertThat(Board.PRODUCT_INQUERY.requiresWriterMasking()).isTrue();
        assertThat(Board.ONE_ON_ONE_INQUERY.requiresWriterMasking()).isFalse();
    }

    @Test
    @DisplayName("resolveCategory에 null을 전달하면 예외가 발생한다")
    void shouldThrowExceptionWhenCategoryCodeIsNull() {
        assertThatThrownBy(() -> Board.NOTICE.resolveCategory(null))
                .isInstanceOf(InvalidPostCategoryForBoardException.class);
    }

    @Test
    @DisplayName("각 Board 상수는 올바른 description을 가진다")
    void shouldHaveCorrectDescription() {
        assertThat(Board.NOTICE.getDescription()).isEqualTo("공지");
        assertThat(Board.FAQ.getDescription()).isEqualTo("FAQ");
        assertThat(Board.PRODUCT_INQUERY.getDescription()).isEqualTo("상품 문의");
        assertThat(Board.ONE_ON_ONE_INQUERY.getDescription()).isEqualTo("1:1 문의");
    }

    @Test
    @DisplayName("각 Board 상수는 올바른 camelCaseValue를 가진다")
    void shouldHaveCorrectCamelCaseValue() {
        assertThat(Board.NOTICE.getCamelCaseValue()).isEqualTo("notice");
        assertThat(Board.FAQ.getCamelCaseValue()).isEqualTo("faq");
        assertThat(Board.PRODUCT_INQUERY.getCamelCaseValue()).isEqualTo("productInquery");
        assertThat(Board.ONE_ON_ONE_INQUERY.getCamelCaseValue()).isEqualTo("oneOnOneInquery");
    }

    @Test
    @DisplayName("각 Board 상수의 술어 메서드는 자기 자신만 true를 반환한다")
    void shouldReturnTrueOnlyForMatchingPredicateMethod() {
        assertThat(Board.NOTICE.isNotice()).isTrue();
        assertThat(Board.NOTICE.isFaq()).isFalse();
        assertThat(Board.NOTICE.isProductInquery()).isFalse();
        assertThat(Board.NOTICE.isOneOnOneInquery()).isFalse();

        assertThat(Board.FAQ.isNotice()).isFalse();
        assertThat(Board.FAQ.isFaq()).isTrue();
        assertThat(Board.FAQ.isProductInquery()).isFalse();
        assertThat(Board.FAQ.isOneOnOneInquery()).isFalse();

        assertThat(Board.PRODUCT_INQUERY.isNotice()).isFalse();
        assertThat(Board.PRODUCT_INQUERY.isFaq()).isFalse();
        assertThat(Board.PRODUCT_INQUERY.isProductInquery()).isTrue();
        assertThat(Board.PRODUCT_INQUERY.isOneOnOneInquery()).isFalse();

        assertThat(Board.ONE_ON_ONE_INQUERY.isNotice()).isFalse();
        assertThat(Board.ONE_ON_ONE_INQUERY.isFaq()).isFalse();
        assertThat(Board.ONE_ON_ONE_INQUERY.isProductInquery()).isFalse();
        assertThat(Board.ONE_ON_ONE_INQUERY.isOneOnOneInquery()).isTrue();
    }
}
