package com.personal.marketnote.community.adapter.in.web.post.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.domain.exception.token.AuthenticationFailedException;
import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.common.utility.FormatConverter;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.common.utility.Role;
import com.personal.marketnote.community.adapter.in.web.post.controller.apidocs.GetPostApiDocs;
import com.personal.marketnote.community.adapter.in.web.post.controller.apidocs.GetPostsApiDocs;
import com.personal.marketnote.community.adapter.in.web.post.controller.apidocs.GetUserOneOnOneInquiryPostsApiDocs;
import com.personal.marketnote.community.adapter.in.web.post.controller.apidocs.GetUserProductInquiryPostsApiDocs;
import com.personal.marketnote.community.adapter.in.web.post.controller.apidocs.RegisterPostApiDocs;
import com.personal.marketnote.community.adapter.in.web.post.controller.apidocs.UpdatePostApiDocs;
import com.personal.marketnote.community.adapter.in.web.post.mapper.PostRequestToCommandMapper;
import com.personal.marketnote.community.adapter.in.web.post.request.RegisterPostRequest;
import com.personal.marketnote.community.adapter.in.web.post.request.UpdatePostRequest;
import com.personal.marketnote.community.adapter.in.web.post.response.GetPostsResponse;
import com.personal.marketnote.community.adapter.in.web.post.response.GetUserOneOnOneInquiryPostsResponse;
import com.personal.marketnote.community.adapter.in.web.post.response.GetUserProductInquiryPostsResponse;
import com.personal.marketnote.community.adapter.in.web.post.response.PostItemResponse;
import com.personal.marketnote.community.adapter.in.web.post.response.RegisterPostResponse;
import com.personal.marketnote.community.domain.post.*;
import com.personal.marketnote.community.port.in.command.post.GetPostQuery;
import com.personal.marketnote.community.port.in.command.post.GetPostsQuery;
import com.personal.marketnote.community.port.in.command.post.GetUserOneOnOneInquiryPostsCommand;
import com.personal.marketnote.community.port.in.command.post.GetUserProductInquiryPostsCommand;
import com.personal.marketnote.community.port.in.result.post.GetPostsResult;
import com.personal.marketnote.community.port.in.result.post.GetUserOneOnOneInquiryPostsResult;
import com.personal.marketnote.community.port.in.result.post.GetUserProductInquiryPostsResult;
import com.personal.marketnote.community.port.in.result.post.PostItemResult;
import com.personal.marketnote.community.port.in.result.post.RegisterPostResult;
import com.personal.marketnote.community.port.in.usecase.post.GetPostUseCase;
import com.personal.marketnote.community.port.in.usecase.post.GetUserOneOnOneInquiryPostsUseCase;
import com.personal.marketnote.community.port.in.usecase.post.GetUserProductInquiryPostsUseCase;
import com.personal.marketnote.community.port.in.usecase.post.RegisterPostUseCase;
import com.personal.marketnote.community.port.in.usecase.post.UpdatePostUseCase;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.domain.exception.ExceptionMessage.INVALID_ACCESS_TOKEN_EXCEPTION_MESSAGE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 게시글 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-13
 * @Description 게시글 관련 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "게시글 API", description = "게시글 관련 API")
@RequiredArgsConstructor
public class PostController {
    private static final String DEFAULT_PAGE_SIZE = "10";

    private final RegisterPostUseCase registerPostUseCase;
    private final GetPostUseCase getPostUseCase;
    private final GetUserProductInquiryPostsUseCase getUserProductInquiryPostsUseCase;
    private final GetUserOneOnOneInquiryPostsUseCase getUserOneOnOneInquiryPostsUseCase;
    private final UpdatePostUseCase updatePostUseCase;

    /**
     * 게시글 등록
     *
     * @param request   게시글 등록 요청
     * @param principal 인증된 사용자 정보
     * @return 게시글 등록 응답 {@link RegisterPostResponse}
     * @Author 성효빈
     * @Date 2026-01-13
     * @Description 게시글을 등록합니다.
     */
    @PostMapping
    @RegisterPostApiDocs
    public ResponseEntity<BaseResponse<RegisterPostResponse>> registerPost(
            @Valid @RequestBody RegisterPostRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        boolean isSeller = validateAuthentication(request, principal);

        RegisterPostResult result = registerPostUseCase.registerPost(
                isSeller, PostRequestToCommandMapper.mapToCommand(request, ElementExtractor.extractUserId(principal))
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        RegisterPostResponse.from(result),
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "게시글 등록 성공"
                ),
                HttpStatus.CREATED
        );
    }

    private boolean validateAuthentication(RegisterPostRequest request, OAuth2AuthenticatedPrincipal principal) {
        List<String> authorities = principal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        boolean isAdmin = authorities.stream()
                .anyMatch(Role::isAdmin);
        boolean isSeller = authorities.stream()
                .anyMatch(Role::isSeller);
        request.validate(isAdmin, isSeller);

        return isSeller;
    }

    /**
     * 게시글 목록 조회
     *
     * @param board          게시판
     * @param category       게시글 카테고리
     * @param targetType     게시글 대상 도메인 유형
     * @param targetId       게시글 대상 ID
     * @param cursor         커서(무한 스크롤 페이지 설정)
     * @param pageSize       페이지 크기
     * @param sortDirection  정렬 방향
     * @param sortProperty   정렬 기준
     * @param searchTarget   검색 대상
     * @param searchKeyword  검색 키워드
     * @param filterCategory 필터 카테고리
     * @param filterValue    필터 값
     * @param principal      인증된 사용자 정보
     * @return 게시글 목록 조회 응답 {@link GetPostsResponse}
     * @Author 성효빈
     * @Date 2025-12-06
     * @Description 게시글 목록을 조회합니다.
     */
    @GetMapping
    @GetPostsApiDocs
    public ResponseEntity<BaseResponse<GetPostsResponse>> getPosts(
            @RequestParam("board") Board board,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "targetGroupType", required = false) PostTargetGroupType targetGroupType,
            @RequestParam(value = "targetGroupId", required = false) Long targetGroupId,
            @RequestParam(value = "targetType", required = false) PostTargetType targetType,
            @RequestParam(value = "targetId", required = false) Long targetId,
            @RequestParam(value = "cursor", required = false) Long cursor,
            @RequestParam(value = "pageSize", required = false, defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(required = false, defaultValue = "ID") PostSortProperty sortProperty,
            @RequestParam(value = "searchTarget", required = false) PostSearchTarget searchTarget,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "filterCategory", required = false) PostFilterCategory filterCategory,
            @RequestParam(value = "filterValue", required = false) PostFilterValue filterValue,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        validateAuthentication(board, targetType, principal);
        Long userId = null;
        if (FormatValidator.hasValue(principal)) {
            userId = ElementExtractor.extractUserId(principal);
        }

        GetPostsResult result = getPostUseCase.getPosts(
                GetPostsQuery.builder()
                        .principal(principal)
                        .userId(userId)
                        .board(board)
                        .category(category)
                        .targetGroupType(targetGroupType)
                        .targetGroupId(targetGroupId)
                        .targetType(targetType)
                        .targetId(targetId)
                        .cursor(cursor)
                        .pageSize(pageSize)
                        .sortDirection(sortDirection)
                        .sortProperty(sortProperty)
                        .searchTarget(searchTarget)
                        .searchKeyword(FormatConverter.toLowerCase(searchKeyword))
                        .filter(filterCategory)
                        .filterValue(filterValue)
                        .build()
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetPostsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "게시글 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 게시글 상세 정보 조회
     *
     * @param board      게시판
     * @param targetType 게시글 대상 도메인 유형
     * @param id         게시글 ID
     * @param principal  인증된 사용자 정보
     * @return 게시글 상세 정보 조회 응답 {@link PostItemResponse}
     * @Author 성효빈
     * @Date 2026-01-27
     * @Description 게시글 상세 정보를 조회합니다.
     */
    @GetMapping("/{id}")
    @GetPostApiDocs
    public ResponseEntity<BaseResponse<PostItemResponse>> getPost(
            @RequestParam("board") Board board,
            @RequestParam(value = "targetType", required = false) PostTargetType targetType,
            @PathVariable("id") Long id,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        validateAuthentication(board, targetType, principal);
        Long userId = null;
        if (FormatValidator.hasValue(principal)) {
            userId = ElementExtractor.extractUserId(principal);
        }

        PostItemResult result = getPostUseCase.getPost(
                GetPostQuery.builder()
                        .principal(principal)
                        .userId(userId)
                        .board(board)
                        .targetType(targetType)
                        .id(id)
                        .build()
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        PostItemResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "게시글 정보 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    private void validateAuthentication(Board board, PostTargetType targetType, OAuth2AuthenticatedPrincipal principal) {
        // 비회원 전용 게시판이거나 상품 상세 정보의 문의 게시판인 경우 인증 제외
        if (board.isNonMemberViewBoard() || FormatValidator.hasValue(targetType)) {
            return;
        }

        if (FormatValidator.hasNoValue(principal) || FormatValidator.equals(principal.getName(), "-1")) {
            throw new AuthenticationFailedException(INVALID_ACCESS_TOKEN_EXCEPTION_MESSAGE);
        }
    }

    /**
     * (관리자) 회원 상품 문의 내역 조회
     *
     * @param userId        회원 ID
     * @param page          페이지 번호 (1부터 시작)
     * @param pageSize      페이지 크기
     * @param sortDirection 정렬 방향
     * @param sortProperty  정렬 기준
     * @return 회원 상품 문의 내역 조회 응답 {@link GetUserProductInquiryPostsResponse}
     * @Author 성효빈
     * @Date 2026-04-01
     * @Description 관리자가 특정 회원의 상품 문의 내역을 조회합니다.
     */
    @GetMapping("/admin/users/{userId}/product-inquiries")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetUserProductInquiryPostsApiDocs
    public ResponseEntity<BaseResponse<GetUserProductInquiryPostsResponse>> getUserProductInquiryPosts(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "page-size", required = false, defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sort-direction", required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(value = "sort-property", required = false, defaultValue = "ID") PostSortProperty sortProperty
    ) {
        GetUserProductInquiryPostsResult result = getUserProductInquiryPostsUseCase.getUserProductInquiryPosts(
                new GetUserProductInquiryPostsCommand(userId, page, pageSize, sortDirection, sortProperty)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetUserProductInquiryPostsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 상품 문의 내역 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 회원 1:1 문의 내역 조회
     *
     * @param userId        회원 ID
     * @param page          페이지 번호 (1부터 시작)
     * @param pageSize      페이지 크기
     * @param sortDirection 정렬 방향
     * @param sortProperty  정렬 기준
     * @return 회원 1:1 문의 내역 조회 응답 {@link GetUserOneOnOneInquiryPostsResponse}
     * @Author 성효빈
     * @Date 2026-04-01
     * @Description 관리자가 특정 회원의 1:1 문의 내역을 조회합니다.
     */
    @GetMapping("/admin/users/{userId}/one-on-one-inquiries")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetUserOneOnOneInquiryPostsApiDocs
    public ResponseEntity<BaseResponse<GetUserOneOnOneInquiryPostsResponse>> getUserOneOnOneInquiryPosts(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "page-size", required = false, defaultValue = DEFAULT_PAGE_SIZE) int pageSize,
            @RequestParam(value = "sort-direction", required = false, defaultValue = "DESC") Sort.Direction sortDirection,
            @RequestParam(value = "sort-property", required = false, defaultValue = "ID") PostSortProperty sortProperty
    ) {
        GetUserOneOnOneInquiryPostsResult result = getUserOneOnOneInquiryPostsUseCase.getUserOneOnOneInquiryPosts(
                new GetUserOneOnOneInquiryPostsCommand(userId, page, pageSize, sortDirection, sortProperty)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetUserOneOnOneInquiryPostsResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "회원 1:1 문의 내역 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * (관리자) 게시글 수정
     *
     * @param request 게시글 수정 요청
     * @Author 성효빈
     * @Date 2026-01-15
     * @Description 게시글 제목/내용을 수정합니다.
     */
    @PatchMapping("/{id}")
    @PreAuthorize(ADMIN_POINTCUT)
    @UpdatePostApiDocs
    public ResponseEntity<BaseResponse<Void>> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostRequest request
    ) {
        updatePostUseCase.updatePost(
                PostRequestToCommandMapper.mapToCommand(id, request)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "게시글 수정 성공"
                ),
                HttpStatus.OK
        );
    }
}
