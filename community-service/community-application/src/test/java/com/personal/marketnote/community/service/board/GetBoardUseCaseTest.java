package com.personal.marketnote.community.service.board;

import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.port.in.result.board.BoardCategoryItemResult;
import com.personal.marketnote.community.port.in.result.board.BoardItemResult;
import com.personal.marketnote.community.port.in.result.board.GetBoardCategoriesResult;
import com.personal.marketnote.community.port.in.result.board.GetBoardsResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GetBoardUseCaseTest {
    private final GetBoardService getBoardService = new GetBoardService();

    @Test
    @DisplayName("게시판 목록 조회 시 null이 아닌 결과를 반환한다")
    void getBoards_returnsNonNullResult() {
        GetBoardsResult result = getBoardService.getBoards();

        assertThat(result).isNotNull();
        assertThat(result.boards()).isNotNull();
    }

    @Test
    @DisplayName("게시판 목록 조회 시 Board 열거형에 선언된 모든 게시판을 반환한다")
    void getBoards_returnsAllDeclaredBoards() {
        GetBoardsResult result = getBoardService.getBoards();

        assertThat(result.boards()).hasSize(Board.values().length);
    }

    @Test
    @DisplayName("게시판 목록의 순서가 Board 열거형 선언 순서(NOTICE → FAQ → PRODUCT_INQUERY → ONE_ON_ONE_INQUERY)와 일치한다")
    void getBoards_orderMatchesBoardEnumDeclarationOrder() {
        GetBoardsResult result = getBoardService.getBoards();

        assertThat(result.boards())
                .extracting(BoardItemResult::name)
                .containsExactly("NOTICE", "FAQ", "PRODUCT_INQUERY", "ONE_ON_ONE_INQUERY");
    }

    @Test
    @DisplayName("각 게시판의 name은 Board 열거형의 name()과 일치한다")
    void getBoards_eachNameMatchesBoardEnumName() {
        GetBoardsResult result = getBoardService.getBoards();
        Board[] boards = Board.values();

        for (int i = 0; i < boards.length; i++) {
            assertThat(result.boards().get(i).name()).isEqualTo(boards[i].name());
        }
    }

    @Test
    @DisplayName("각 게시판의 description은 Board 열거형의 description과 일치한다")
    void getBoards_eachDescriptionMatchesBoardEnumDescription() {
        GetBoardsResult result = getBoardService.getBoards();
        Board[] boards = Board.values();

        for (int i = 0; i < boards.length; i++) {
            assertThat(result.boards().get(i).description()).isEqualTo(boards[i].getDescription());
        }
    }

    @Test
    @DisplayName("NOTICE 게시판의 설명은 '공지'이다")
    void getBoards_noticeBoard_hasCorrectDescription() {
        GetBoardsResult result = getBoardService.getBoards();
        BoardItemResult notice = findBoardByName(result.boards(), "NOTICE");

        assertThat(notice).isNotNull();
        assertThat(notice.description()).isEqualTo("공지");
    }

    @Test
    @DisplayName("FAQ 게시판의 설명은 'FAQ'이다")
    void getBoards_faqBoard_hasCorrectDescription() {
        GetBoardsResult result = getBoardService.getBoards();
        BoardItemResult faq = findBoardByName(result.boards(), "FAQ");

        assertThat(faq).isNotNull();
        assertThat(faq.description()).isEqualTo("FAQ");
    }

    @Test
    @DisplayName("PRODUCT_INQUERY 게시판의 설명은 '상품 문의'이다")
    void getBoards_productInqueryBoard_hasCorrectDescription() {
        GetBoardsResult result = getBoardService.getBoards();
        BoardItemResult productInquery = findBoardByName(result.boards(), "PRODUCT_INQUERY");

        assertThat(productInquery).isNotNull();
        assertThat(productInquery.description()).isEqualTo("상품 문의");
    }

    @Test
    @DisplayName("ONE_ON_ONE_INQUERY 게시판의 설명은 '1:1 문의'이다")
    void getBoards_oneOnOneInqueryBoard_hasCorrectDescription() {
        GetBoardsResult result = getBoardService.getBoards();
        BoardItemResult oneOnOneInquery = findBoardByName(result.boards(), "ONE_ON_ONE_INQUERY");

        assertThat(oneOnOneInquery).isNotNull();
        assertThat(oneOnOneInquery.description()).isEqualTo("1:1 문의");
    }

    @Test
    @DisplayName("게시판 목록의 각 항목은 name과 description이 모두 비어 있지 않다")
    void getBoards_allFieldsAreNonBlank() {
        GetBoardsResult result = getBoardService.getBoards();

        result.boards().forEach(board -> {
            assertThat(board.name()).isNotBlank();
            assertThat(board.description()).isNotBlank();
        });
    }

    @Test
    @DisplayName("게시판 목록에 중복된 게시판이 존재하지 않는다")
    void getBoards_noDuplicateEntries() {
        GetBoardsResult result = getBoardService.getBoards();

        assertThat(result.boards())
                .extracting(BoardItemResult::name)
                .doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("여러 번 호출해도 동일한 결과를 반환한다")
    void getBoards_multipleInvocations_returnConsistentResult() {
        GetBoardsResult first = getBoardService.getBoards();
        GetBoardsResult second = getBoardService.getBoards();

        assertThat(first.boards()).isEqualTo(second.boards());
    }

    @Test
    @DisplayName("모든 Board 열거형 값에 대해 카테고리 조회가 성공한다")
    void getCategories_allBoards_returnNonNullResult() {
        for (Board board : Board.values()) {
            GetBoardCategoriesResult result = getBoardService.getCategories(board);

            assertThat(result).isNotNull();
            assertThat(result.categories()).isNotNull();
            assertThat(result.categories()).isNotEmpty();
        }
    }

    @Test
    @DisplayName("NOTICE 게시판의 카테고리는 2개(ANNOUNCEMENT, EVENT)이다")
    void getCategories_notice_returnsTwoCategories() {
        GetBoardCategoriesResult result = getBoardService.getCategories(Board.NOTICE);

        assertThat(result.categories()).hasSize(2);
        assertThat(result.categories())
                .extracting(BoardCategoryItemResult::name)
                .containsExactly("ANNOUNCEMENT", "EVENT");
    }

    @Test
    @DisplayName("NOTICE 게시판의 각 카테고리 설명이 정확하다")
    void getCategories_notice_hasCorrectDescriptions() {
        GetBoardCategoriesResult result = getBoardService.getCategories(Board.NOTICE);

        assertThat(result.categories())
                .extracting(BoardCategoryItemResult::description)
                .containsExactly("공지", "이벤트");
    }

    @Test
    @DisplayName("FAQ 게시판의 카테고리는 7개이다")
    void getCategories_faq_returnsSevenCategories() {
        GetBoardCategoriesResult result = getBoardService.getCategories(Board.FAQ);

        assertThat(result.categories()).hasSize(7);
        assertThat(result.categories())
                .extracting(BoardCategoryItemResult::name)
                .containsExactly(
                        "ORDER_PAYMENT", "DELIVERY", "CANCEL_REFUND",
                        "RETURN_EXCHANGE", "POINT", "EVENT_COUPON", "LOGIN_USER_INFO"
                );
    }

    @Test
    @DisplayName("FAQ 게시판의 각 카테고리 설명이 정확하다")
    void getCategories_faq_hasCorrectDescriptions() {
        GetBoardCategoriesResult result = getBoardService.getCategories(Board.FAQ);

        assertThat(result.categories())
                .extracting(BoardCategoryItemResult::description)
                .containsExactly(
                        "주문/결제", "배송 관련", "취소/환불",
                        "반품/교환", "적립금(포인트)", "이벤트/쿠폰", "로그인/회원정보"
                );
    }

    @Test
    @DisplayName("PRODUCT_INQUERY 게시판의 카테고리는 3개(PRODUCT_QUESTION, RESTOCK, SHIPPING)이다")
    void getCategories_productInquery_returnsThreeCategories() {
        GetBoardCategoriesResult result = getBoardService.getCategories(Board.PRODUCT_INQUERY);

        assertThat(result.categories()).hasSize(3);
        assertThat(result.categories())
                .extracting(BoardCategoryItemResult::name)
                .containsExactly("PRODUCT_QUESTION", "RESTOCK", "SHIPPING");
    }

    @Test
    @DisplayName("PRODUCT_INQUERY 게시판의 각 카테고리 설명이 정확하다")
    void getCategories_productInquery_hasCorrectDescriptions() {
        GetBoardCategoriesResult result = getBoardService.getCategories(Board.PRODUCT_INQUERY);

        assertThat(result.categories())
                .extracting(BoardCategoryItemResult::description)
                .containsExactly("상품 문의", "재입고 문의", "배송 문의");
    }

    @Test
    @DisplayName("ONE_ON_ONE_INQUERY 게시판의 카테고리는 6개이다")
    void getCategories_oneOnOneInquery_returnsSixCategories() {
        GetBoardCategoriesResult result = getBoardService.getCategories(Board.ONE_ON_ONE_INQUERY);

        assertThat(result.categories()).hasSize(6);
        assertThat(result.categories())
                .extracting(BoardCategoryItemResult::name)
                .containsExactly(
                        "ORDER_PAYMENT", "DELIVERY", "CANCEL_REFUND",
                        "RETURN_EXCHANGE", "POINT", "EVENT_COUPON"
                );
    }

    @Test
    @DisplayName("ONE_ON_ONE_INQUERY 게시판의 각 카테고리 설명이 정확하다")
    void getCategories_oneOnOneInquery_hasCorrectDescriptions() {
        GetBoardCategoriesResult result = getBoardService.getCategories(Board.ONE_ON_ONE_INQUERY);

        assertThat(result.categories())
                .extracting(BoardCategoryItemResult::description)
                .containsExactly(
                        "주문/결제", "배송 관련", "취소/환불",
                        "반품/교환", "적립금(포인트)", "이벤트/쿠폰"
                );
    }

    @Test
    @DisplayName("각 게시판의 카테고리 항목은 name과 description이 모두 비어 있지 않다")
    void getCategories_allBoards_allFieldsAreNonBlank() {
        for (Board board : Board.values()) {
            GetBoardCategoriesResult result = getBoardService.getCategories(board);

            result.categories().forEach(category -> {
                assertThat(category.name())
                        .as("%s 게시판의 카테고리 name", board.name())
                        .isNotBlank();
                assertThat(category.description())
                        .as("%s 게시판의 카테고리 description", board.name())
                        .isNotBlank();
            });
        }
    }

    @Test
    @DisplayName("각 게시판의 카테고리 목록에 중복 항목이 존재하지 않는다")
    void getCategories_allBoards_noDuplicateEntries() {
        for (Board board : Board.values()) {
            GetBoardCategoriesResult result = getBoardService.getCategories(board);

            assertThat(result.categories())
                    .as("%s 게시판의 카테고리 중복 검사", board.name())
                    .extracting(BoardCategoryItemResult::name)
                    .doesNotHaveDuplicates();
        }
    }

    @Test
    @DisplayName("서로 다른 게시판의 카테고리 개수가 각각 다르다")
    void getCategories_differentBoards_haveDifferentCategoryCounts() {
        int noticeCount = getBoardService.getCategories(Board.NOTICE).categories().size();
        int faqCount = getBoardService.getCategories(Board.FAQ).categories().size();
        int productInqueryCount = getBoardService.getCategories(Board.PRODUCT_INQUERY).categories().size();
        int oneOnOneCount = getBoardService.getCategories(Board.ONE_ON_ONE_INQUERY).categories().size();

        assertThat(noticeCount).isEqualTo(2);
        assertThat(faqCount).isEqualTo(7);
        assertThat(productInqueryCount).isEqualTo(3);
        assertThat(oneOnOneCount).isEqualTo(6);
    }

    @Test
    @DisplayName("동일 게시판을 여러 번 조회해도 동일한 카테고리 결과를 반환한다")
    void getCategories_multipleInvocations_returnConsistentResult() {
        for (Board board : Board.values()) {
            GetBoardCategoriesResult first = getBoardService.getCategories(board);
            GetBoardCategoriesResult second = getBoardService.getCategories(board);

            assertThat(first.categories())
                    .as("%s 게시판의 멱등성 검사", board.name())
                    .isEqualTo(second.categories());
        }
    }

    @Test
    @DisplayName("FAQ와 ONE_ON_ONE_INQUERY는 공통 카테고리(ORDER_PAYMENT 등)를 공유하지만 FAQ에만 LOGIN_USER_INFO가 있다")
    void getCategories_faqAndOneOnOne_shareCommonCategoriesButFaqHasExtra() {
        GetBoardCategoriesResult faqResult = getBoardService.getCategories(Board.FAQ);
        GetBoardCategoriesResult oneOnOneResult = getBoardService.getCategories(Board.ONE_ON_ONE_INQUERY);

        List<String> faqNames = faqResult.categories().stream()
                .map(BoardCategoryItemResult::name).toList();
        List<String> oneOnOneNames = oneOnOneResult.categories().stream()
                .map(BoardCategoryItemResult::name).toList();

        assertThat(faqNames).containsAll(oneOnOneNames);
        assertThat(faqNames).contains("LOGIN_USER_INFO");
        assertThat(oneOnOneNames).doesNotContain("LOGIN_USER_INFO");
    }

    private BoardItemResult findBoardByName(List<BoardItemResult> boards, String name) {
        return boards.stream()
                .filter(board -> board.name().equals(name))
                .findFirst()
                .orElse(null);
    }
}
