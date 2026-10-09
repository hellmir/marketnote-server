package com.personal.marketnote.community.adapter.in.web.board;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.community.adapter.in.web.post.response.GetBoardCategoriesResponse;
import com.personal.marketnote.community.adapter.in.web.post.response.GetBoardsResponse;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.port.in.result.board.GetBoardCategoriesResult;
import com.personal.marketnote.community.port.in.result.board.GetBoardsResult;
import com.personal.marketnote.community.port.in.usecase.board.GetBoardUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardController")
class BoardControllerTest {

    @Mock
    private GetBoardUseCase getBoardUseCase;

    @InjectMocks
    private BoardController controller;

    @Nested
    @DisplayName("getBoards")
    class GetBoards {

        @Test
        @DisplayName("UseCase의 게시판 목록을 조회해 OK 응답으로 감싸 반환한다")
        void returnsBoardsWrappedAsOkResponse() {
            GetBoardsResult result = GetBoardsResult.from(Board.values());
            when(getBoardUseCase.getBoards()).thenReturn(result);

            ResponseEntity<BaseResponse<GetBoardsResponse>> response = controller.getBoards();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getCode()).isEqualTo("SUC01");
            assertThat(response.getBody().getContent()).isNotNull();
            assertThat(response.getBody().getContent().categories()).hasSize(Board.values().length);
            verify(getBoardUseCase).getBoards();
        }

        @Test
        @DisplayName("UseCase가 빈 목록을 반환하면 OK 응답의 content에 빈 목록이 담긴다")
        void returnsEmptyBoardsWhenUseCaseReturnsEmpty() {
            when(getBoardUseCase.getBoards()).thenReturn(new GetBoardsResult(List.of()));

            ResponseEntity<BaseResponse<GetBoardsResponse>> response = controller.getBoards();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody().getContent().categories()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCategories")
    class GetCategories {

        @Test
        @DisplayName("NOTICE 게시판의 카테고리 목록을 조회해 OK 응답으로 감싸 반환한다")
        void returnsNoticeCategories() {
            GetBoardCategoriesResult result = GetBoardCategoriesResult.from(
                    com.personal.marketnote.community.domain.post.NoticePostCategory.values()
            );
            when(getBoardUseCase.getCategories(Board.NOTICE)).thenReturn(result);

            ResponseEntity<BaseResponse<GetBoardCategoriesResponse>> response =
                    controller.getCategories(Board.NOTICE);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getContent()).isNotNull();
            verify(getBoardUseCase).getCategories(Board.NOTICE);
        }

        @Test
        @DisplayName("FAQ 게시판의 카테고리 목록도 동일하게 위임한다")
        void returnsFaqCategories() {
            GetBoardCategoriesResult result = GetBoardCategoriesResult.from(
                    com.personal.marketnote.community.domain.post.FaqPostCategory.values()
            );
            when(getBoardUseCase.getCategories(Board.FAQ)).thenReturn(result);

            ResponseEntity<BaseResponse<GetBoardCategoriesResponse>> response =
                    controller.getCategories(Board.FAQ);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(getBoardUseCase).getCategories(Board.FAQ);
        }
    }
}
