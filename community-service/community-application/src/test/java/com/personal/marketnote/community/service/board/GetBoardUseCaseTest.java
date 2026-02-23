package com.personal.marketnote.community.service.board;

import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.port.in.result.board.BoardItemResult;
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

    private BoardItemResult findBoardByName(List<BoardItemResult> boards, String name) {
        return boards.stream()
                .filter(board -> board.name().equals(name))
                .findFirst()
                .orElse(null);
    }
}
