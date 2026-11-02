package com.personal.marketnote.community.adapter.in.web.board;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.community.adapter.in.web.board.apidocs.GetBoardCategoriesApiDocs;
import com.personal.marketnote.community.adapter.in.web.board.apidocs.GetBoardsApiDocs;
import com.personal.marketnote.community.adapter.in.web.post.response.GetBoardCategoriesResponse;
import com.personal.marketnote.community.adapter.in.web.post.response.GetBoardsResponse;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.port.in.result.board.GetBoardCategoriesResult;
import com.personal.marketnote.community.port.in.result.board.GetBoardsResult;
import com.personal.marketnote.community.port.in.usecase.board.GetBoardUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;

@RestController
@RequestMapping("/api/v1/boards")
@Tag(name = "게시판 API", description = "게시판 관련 API")
@RequiredArgsConstructor
public class BoardController {
    private final GetBoardUseCase getBoardUseCase;

    /**
     * (비회원) 게시판 목록 조회
     *
     * @return 게시판 목록 조회 응답 {@link GetBoardCategoriesResponse}
     * @Author 성효빈
     * @Date 2026-01-15
     * @Description 게시판 목록을 조회합니다.
     */
    @GetMapping
    @GetBoardsApiDocs
    public ResponseEntity<BaseResponse<GetBoardsResponse>> getBoards() {
        GetBoardsResult result = getBoardUseCase.getBoards();

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetBoardsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "게시판 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (비회원) 게시판 카테고리 목록 조회
     *
     * @param board 게시판
     * @return 게시판 카테고리 목록 조회 응답 {@link GetBoardCategoriesResponse}
     * @Author 성효빈
     * @Date 2026-01-15
     * @Description 게시판 카테고리 목록을 조회합니다.
     */
    @GetMapping("/categories")
    @GetBoardCategoriesApiDocs
    public ResponseEntity<BaseResponse<GetBoardCategoriesResponse>> getCategories(
            @RequestParam("board") Board board
    ) {
        GetBoardCategoriesResult result = getBoardUseCase.getCategories(board);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetBoardCategoriesResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "게시판 카테고리 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
