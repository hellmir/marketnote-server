package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.community.domain.post.*;
import com.personal.marketnote.community.port.in.command.post.GetPostsQuery;
import com.personal.marketnote.community.port.in.result.post.GetPostsResult;
import com.personal.marketnote.community.port.in.result.post.PostItemResult;
import com.personal.marketnote.community.port.out.file.FindPostImagesPort;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.personal.marketnote.common.domain.file.FileSort.POST_IMAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPostsUseCaseTest {
    @Mock
    private FindPostPort findPostPort;
    @Mock
    private FindProductByPricePolicyPort findProductByPricePolicyPort;
    @Mock
    private FindPostImagesPort findPostImagesPort;

    @InjectMocks
    private GetPostService getPostService;

    // ========== A. 조회 분기 ==========

    @Test
    @DisplayName("NOTICE 게시판 조회 시 findPublicPosts가 호출된다")
    void getPosts_noticeBoard_callsFindPublicPosts() {
        GetPostsQuery query = publicQuery(Board.NOTICE).build();
        mockPublicPosts(emptyPosts());

        getPostService.getPosts(query);

        verifyPublicPostsCalled();
        verifyUserPostsNeverCalled();
    }

    @Test
    @DisplayName("FAQ 게시판 조회 시 findPublicPosts가 호출된다")
    void getPosts_faqBoard_callsFindPublicPosts() {
        GetPostsQuery query = publicQuery(Board.FAQ).build();
        mockPublicPosts(emptyPosts());

        getPostService.getPosts(query);

        verifyPublicPostsCalled();
        verifyUserPostsNeverCalled();
    }

    @Test
    @DisplayName("targetType이 있는 PRODUCT_INQUERY 조회 시 findPublicPosts가 호출된다")
    void getPosts_productInqueryWithTargetType_callsFindPublicPosts() {
        GetPostsQuery query = productInqueryPublicQuery().build();
        mockPublicPosts(emptyPosts());
        mockProductInfoPort(Map.of());

        getPostService.getPosts(query);

        verifyPublicPostsCalled();
        verifyUserPostsNeverCalled();
    }

    @Test
    @DisplayName("targetType이 없는 PRODUCT_INQUERY 조회 시 findUserPosts가 호출된다")
    void getPosts_productInqueryWithoutTargetType_callsFindUserPosts() {
        GetPostsQuery query = productInqueryUserQuery().build();
        mockUserPosts(emptyPosts());
        mockProductInfoPort(Map.of());

        getPostService.getPosts(query);

        verifyUserPostsCalled();
        verifyPublicPostsNeverCalled();
    }

    @Test
    @DisplayName("ONE_ON_ONE_INQUERY 조회 시 findUserPosts가 호출된다")
    void getPosts_oneOnOneInquery_callsFindUserPosts() {
        GetPostsQuery query = userQuery().build();
        mockUserPosts(emptyPosts());

        getPostService.getPosts(query);

        verifyUserPostsCalled();
        verifyPublicPostsNeverCalled();
    }

    // ========== B. 정렬 ==========

    @Test
    @DisplayName("sortDirection이 null이면 기본값 DESC가 적용된다")
    void getPosts_nullSortDirection_appliesDescByDefault() {
        GetPostsQuery query = publicQuery(Board.NOTICE)
                .sortDirection(null)
                .build();
        mockPublicPosts(emptyPosts());

        getPostService.getPosts(query);

        Pageable pageable = capturePublicPostsPageable();
        Sort.Order order = pageable.getSort().iterator().next();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("sortDirection이 ASC이면 ASC 정렬이 적용된다")
    void getPosts_ascSortDirection_appliesAsc() {
        GetPostsQuery query = publicQuery(Board.NOTICE)
                .sortDirection(Sort.Direction.ASC)
                .build();
        mockPublicPosts(emptyPosts());

        getPostService.getPosts(query);

        Pageable pageable = capturePublicPostsPageable();
        Sort.Order order = pageable.getSort().iterator().next();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    @DisplayName("공개 게시판(NOTICE)은 sortProperty가 ORDER_NUM으로 설정되어 orderNum 필드로 정렬한다")
    void getPosts_noticeBoard_sortsByOrderNum() {
        GetPostsQuery query = publicQuery(Board.NOTICE).build();
        mockPublicPosts(emptyPosts());

        getPostService.getPosts(query);

        Pageable pageable = capturePublicPostsPageable();
        Sort.Order order = pageable.getSort().iterator().next();
        assertThat(order.getProperty()).isEqualTo("orderNum");
    }

    @Test
    @DisplayName("비공개 게시판에서 sortProperty 미지정 시 ID가 기본값으로 id 필드로 정렬한다")
    void getPosts_userBoard_noSortProperty_defaultsToId() {
        GetPostsQuery query = userQuery()
                .sortProperty(null)
                .build();
        mockUserPosts(emptyPosts());

        getPostService.getPosts(query);

        Pageable pageable = captureUserPostsPageable();
        Sort.Order order = pageable.getSort().iterator().next();
        assertThat(order.getProperty()).isEqualTo("id");
    }

    @Test
    @DisplayName("비공개 게시판에서 IS_ANSWERED sortProperty가 적용되면 id 필드로 정렬한다")
    void getPosts_userBoard_isAnsweredSort_mapsToIdField() {
        GetPostsQuery query = userQuery()
                .sortProperty(PostSortProperty.IS_ANSWERED)
                .build();
        mockUserPosts(emptyPosts());

        getPostService.getPosts(query);

        Pageable pageable = captureUserPostsPageable();
        Sort.Order order = pageable.getSort().iterator().next();
        assertThat(order.getProperty()).isEqualTo("id");
    }

    // ========== C. 페이지네이션 ==========

    @Test
    @DisplayName("결과 수가 pageSize보다 많으면 hasNext가 true이고 결과가 pageSize만큼 잘린다")
    void getPosts_moreThanPageSize_hasNextTrueAndTruncated() {
        int pageSize = 2;
        GetPostsQuery query = publicQuery(Board.NOTICE)
                .pageSize(pageSize)
                .build();
        Posts posts = Posts.from(List.of(
                buildNoticePost(10L),
                buildNoticePost(11L),
                buildNoticePost(12L)
        ));
        mockPublicPosts(posts);

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.hasNext()).isTrue();
        assertThat(result.posts()).hasSize(pageSize);
    }

    @Test
    @DisplayName("결과 수가 pageSize 이하이면 hasNext가 false이다")
    void getPosts_lessThanOrEqualToPageSize_hasNextFalse() {
        GetPostsQuery query = publicQuery(Board.NOTICE)
                .pageSize(10)
                .build();
        Posts posts = Posts.from(List.of(
                buildNoticePost(10L),
                buildNoticePost(11L)
        ));
        mockPublicPosts(posts);

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.hasNext()).isFalse();
        assertThat(result.posts()).hasSize(2);
    }

    @Test
    @DisplayName("nextCursor는 마지막 게시글의 ID이다")
    void getPosts_nonEmptyResult_nextCursorIsLastPostId() {
        GetPostsQuery query = publicQuery(Board.NOTICE).build();
        Posts posts = Posts.from(List.of(
                buildNoticePost(10L),
                buildNoticePost(20L),
                buildNoticePost(30L)
        ));
        mockPublicPosts(posts);

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.nextCursor()).isEqualTo(30L);
    }

    @Test
    @DisplayName("조회 결과가 비어 있으면 nextCursor가 null이고 빈 목록을 반환한다")
    void getPosts_emptyResult_nextCursorNullAndEmptyList() {
        GetPostsQuery query = publicQuery(Board.NOTICE).build();
        mockPublicPosts(emptyPosts());

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.nextCursor()).isNull();
        assertThat(result.posts()).isEmpty();
    }

    // ========== D. totalElements ==========

    @Test
    @DisplayName("커서가 없으면 공개 게시판의 totalElements가 countPublicPosts로 계산된다")
    void getPosts_noCursor_publicBoard_computesTotalViaCountPublicPosts() {
        GetPostsQuery query = publicQuery(Board.NOTICE)
                .cursor(null)
                .build();
        mockPublicPosts(emptyPosts());
        when(findPostPort.countPublicPosts(
                any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(42L);

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.totalElements()).isEqualTo(42L);
        verify(findPostPort).countPublicPosts(
                any(), any(), any(), any(), any(), any(), any(), any(), any()
        );
    }

    @Test
    @DisplayName("커서가 없으면 회원 게시판의 totalElements가 countUserPosts로 계산된다")
    void getPosts_noCursor_userBoard_computesTotalViaCountUserPosts() {
        GetPostsQuery query = userQuery()
                .cursor(null)
                .build();
        mockUserPosts(emptyPosts());
        when(findPostPort.countUserPosts(any(), any(), any(), any(), any(), any())).thenReturn(99L);

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.totalElements()).isEqualTo(99L);
        verify(findPostPort).countUserPosts(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("커서가 있으면 totalElements가 null이다")
    void getPosts_hasCursor_totalElementsNull() {
        GetPostsQuery query = publicQuery(Board.NOTICE).build();
        mockPublicPosts(emptyPosts());

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.totalElements()).isNull();
        verify(findPostPort, never()).countPublicPosts(
                any(), any(), any(), any(), any(), any(), any(), any(), any()
        );
    }

    // ========== E. 상품 정보 ==========

    @Test
    @DisplayName("PRODUCT_INQUERY 게시판에서 targetId가 있는 게시글이면 상품 정보가 PostItemResult에 포함된다")
    void getPosts_productInquery_withTargetId_includesProductInfo() {
        Long targetId = 100L;
        GetPostsQuery query = productInqueryPublicQuery().build();
        Post post = buildProductInqueryPost(1L, 1L, targetId, false);
        mockPublicPosts(Posts.from(List.of(post)));
        ProductInfoResult productInfo = buildProductInfo(1L);
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                .thenReturn(Map.of(targetId, productInfo));

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getProduct()).isNotNull();
        assertThat(result.posts().getFirst().getProduct().name()).isEqualTo("상품명");
        assertThat(result.posts().getFirst().getProduct().brandName()).isEqualTo("브랜드명");
    }

    @Test
    @DisplayName("PRODUCT_INQUERY가 아니면 FindProductByPricePolicyPort가 호출되지 않는다")
    void getPosts_nonProductInqueryBoard_doesNotCallProductPort() {
        GetPostsQuery query = userQuery().build();
        mockUserPosts(Posts.from(List.of(buildOneOnOnePost(1L))));

        getPostService.getPosts(query);

        verifyNoInteractions(findProductByPricePolicyPort);
    }

    @Test
    @DisplayName("PRODUCT_INQUERY에서 targetId가 없는 게시글은 상품 정보 없이 생성된다")
    void getPosts_productInquery_noTargetId_noProductInfo() {
        GetPostsQuery query = productInqueryUserQuery().build();
        Post post = buildProductInqueryPostWithoutTarget(1L);
        mockUserPosts(Posts.from(List.of(post)));
        mockProductInfoPort(Map.of());

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getProduct()).isNull();
    }

    // ========== F. 비밀글 마스킹 ==========

    @Test
    @DisplayName("targetType이 있고 비밀글이고 본인이 아니며 관리자/판매자가 아니면 마스킹된다")
    void getPosts_privatePost_differentUser_notAdmin_masked() {
        Long targetId = 100L;
        Long postUserId = 999L;
        GetPostsQuery query = productInqueryPublicQuery()
                .principal(null)
                .userId(1L)
                .build();
        Post post = buildProductInqueryPost(1L, postUserId, targetId, true);
        mockPublicPosts(Posts.from(List.of(post)));
        ProductInfoResult productInfo = buildProductInfo(777L);
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                .thenReturn(Map.of(targetId, productInfo));

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
        PostItemResult item = result.posts().getFirst();
        assertThat(item.isMasked()).isTrue();
        assertThat(item.getTitle()).isNull();
        assertThat(item.getContent()).isNull();
    }

    @Test
    @DisplayName("targetType이 있고 비밀글이지만 본인 게시글이면 마스킹되지 않는다")
    void getPosts_privatePost_sameUser_notMasked() {
        Long targetId = 100L;
        Long userId = 1L;
        GetPostsQuery query = productInqueryPublicQuery()
                .principal(null)
                .userId(userId)
                .build();
        Post post = buildProductInqueryPost(1L, userId, targetId, true);
        mockPublicPosts(Posts.from(List.of(post)));
        ProductInfoResult productInfo = buildProductInfo(777L);
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                .thenReturn(Map.of(targetId, productInfo));

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
        PostItemResult item = result.posts().getFirst();
        assertThat(item.isMasked()).isFalse();
        assertThat(item.getTitle()).isNotNull();
    }

    @Test
    @DisplayName("targetType이 없으면 비밀글이어도 마스킹되지 않는다")
    void getPosts_noTargetType_privatePost_notMasked() {
        Long targetId = 100L;
        GetPostsQuery query = productInqueryUserQuery()
                .principal(null)
                .userId(1L)
                .build();
        Post post = buildProductInqueryPost(1L, 999L, targetId, true);
        mockUserPosts(Posts.from(List.of(post)));
        ProductInfoResult productInfo = buildProductInfo(777L);
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                .thenReturn(Map.of(targetId, productInfo));

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
        PostItemResult item = result.posts().getFirst();
        assertThat(item.isMasked()).isFalse();
    }

    // ========== G. 검색 키워드 필터링 ==========

    @Test
    @DisplayName("ONE_ON_ONE_INQUERY에서 제목 검색 시 제목에 키워드가 포함된 게시글만 반환된다")
    void getPosts_oneOnOne_titleSearch_returnsMatchingPosts() {
        GetPostsQuery query = userQuery()
                .searchTarget(PostSearchTarget.TITLE)
                .searchKeyword("배송")
                .build();
        Post matchingPost = buildOneOnOnePostWithContent(1L, "배송 문의합니다", "내용입니다");
        Post nonMatchingPost = buildOneOnOnePostWithContent(2L, "결제 문의", "배송 관련 내용");
        mockUserPosts(Posts.from(List.of(matchingPost, nonMatchingPost)));

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ONE_ON_ONE_INQUERY에서 내용 검색 시 내용에 키워드가 포함된 게시글만 반환된다")
    void getPosts_oneOnOne_contentSearch_returnsMatchingPosts() {
        GetPostsQuery query = userQuery()
                .searchTarget(PostSearchTarget.CONTENT)
                .searchKeyword("환불")
                .build();
        Post matchingPost = buildOneOnOnePostWithContent(1L, "문의 제목", "환불 요청합니다");
        Post nonMatchingPost = buildOneOnOnePostWithContent(2L, "환불 문의", "결제 관련");
        mockUserPosts(Posts.from(List.of(matchingPost, nonMatchingPost)));

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("ONE_ON_ONE_INQUERY에서 검색 대상 미지정 시 제목 또는 내용에서 매칭된다")
    void getPosts_oneOnOne_noSearchTarget_matchesTitleOrContent() {
        GetPostsQuery query = userQuery()
                .searchTarget(null)
                .searchKeyword("포인트")
                .build();
        Post titleMatch = buildOneOnOnePostWithContent(1L, "포인트 적립 문의", "내용입니다");
        Post contentMatch = buildOneOnOnePostWithContent(2L, "일반 문의", "포인트 관련 문의");
        Post noMatch = buildOneOnOnePostWithContent(3L, "배송 문의", "결제 관련");
        mockUserPosts(Posts.from(List.of(titleMatch, contentMatch, noMatch)));

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(2);
        assertThat(result.posts())
                .extracting(PostItemResult::getId)
                .containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("PRODUCT_INQUERY에서 targetType 미지정 시 검색 키워드 필터링이 적용된다")
    void getPosts_productInquery_noTargetType_keywordFilteringApplied() {
        GetPostsQuery query = productInqueryUserQuery()
                .searchTarget(PostSearchTarget.TITLE)
                .searchKeyword("재입고")
                .build();
        Post matchingPost = buildProductInqueryPostWithContent(1L, "재입고 문의", "내용");
        Post nonMatchingPost = buildProductInqueryPostWithContent(2L, "배송 문의", "내용");
        mockUserPosts(Posts.from(List.of(matchingPost, nonMatchingPost)));
        mockProductInfoPort(Map.of());

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("검색 키워드가 없으면 전체 게시글이 반환된다")
    void getPosts_noSearchKeyword_returnsAllPosts() {
        GetPostsQuery query = userQuery()
                .searchKeyword(null)
                .build();
        Post post1 = buildOneOnOnePost(1L);
        Post post2 = buildOneOnOnePost(2L);
        mockUserPosts(Posts.from(List.of(post1, post2)));

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(2);
    }

    @Test
    @DisplayName("검색 키워드 매칭은 대소문자를 무시한다")
    void getPosts_keywordSearch_caseInsensitive() {
        GetPostsQuery query = userQuery()
                .searchTarget(PostSearchTarget.TITLE)
                .searchKeyword("hello")
                .build();
        Post matchingPost = buildOneOnOnePostWithContent(1L, "Hello World", "content");
        Post nonMatchingPost = buildOneOnOnePostWithContent(2L, "Goodbye", "content");
        mockUserPosts(Posts.from(List.of(matchingPost, nonMatchingPost)));

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("PRODUCT_INQUERY에서 targetType이 있으면 인메모리 검색 필터링이 적용되지 않는다")
    void getPosts_productInquery_withTargetType_noInMemoryFiltering() {
        GetPostsQuery query = productInqueryPublicQuery()
                .searchTarget(PostSearchTarget.TITLE)
                .searchKeyword("재입고")
                .build();
        Post post = buildProductInqueryPostWithContent(1L, "배송 문의입니다", "내용");
        mockPublicPosts(Posts.from(List.of(post)));
        mockProductInfoPort(Map.of());

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
    }

    // ========== H. 이미지 ==========

    @Test
    @DisplayName("사진 게시글이면 이미지가 PostItemResult에 포함된다")
    void getPosts_photoPost_includesImages() {
        GetPostsQuery query = publicQuery(Board.NOTICE).build();
        Post photoPost = buildPhotoPost(1L, Board.NOTICE, "ANNOUNCEMENT");
        mockPublicPosts(Posts.from(List.of(photoPost)));
        GetFileResult fileResult = new GetFileResult(
                1L, "POST_IMAGE", "jpg", "image.jpg", "https://s3/image.jpg", List.of(), 1L
        );
        when(findPostImagesPort.findImagesByPostIdAndSort(eq(1L), eq(POST_IMAGE)))
                .thenReturn(Optional.of(new GetFilesResult(List.of(fileResult))));

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getImages()).isNotNull();
        assertThat(result.posts().getFirst().getImages()).hasSize(1);
    }

    @Test
    @DisplayName("사진 게시글이 아니면 FindPostImagesPort가 호출되지 않는다")
    void getPosts_nonPhotoPost_doesNotCallImagePort() {
        GetPostsQuery query = publicQuery(Board.NOTICE).build();
        Post nonPhotoPost = buildNoticePost(1L);
        mockPublicPosts(Posts.from(List.of(nonPhotoPost)));

        getPostService.getPosts(query);

        verifyNoInteractions(findPostImagesPort);
    }

    // ========== I. 답글 ==========

    @Test
    @DisplayName("targetId가 있고 답글이 있으며 마스킹되지 않은 게시글이면 답글이 PostItemResult에 추가된다")
    void getPosts_targetIdWithReplies_notMasked_repliesAdded() {
        Long targetId = 100L;
        Long userId = 1L;
        GetPostsQuery query = productInqueryPublicQuery()
                .principal(null)
                .userId(userId)
                .build();
        Post parentPost = buildProductInqueryPost(1L, userId, targetId, false);
        Post replyPost = buildProductInqueryPost(2L, userId, targetId, false);
        parentPost.addReplies(List.of(replyPost));
        mockPublicPosts(Posts.from(List.of(parentPost)));
        ProductInfoResult productInfo = buildProductInfo(userId);
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                .thenReturn(Map.of(targetId, productInfo));

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getReplies()).isNotNull();
        assertThat(result.posts().getFirst().getReplies()).hasSize(1);
        assertThat(result.posts().getFirst().getReplies().getFirst().getId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("마스킹된 게시글이면 답글이 추가되지 않는다")
    void getPosts_maskedPost_repliesNotAdded() {
        Long targetId = 100L;
        GetPostsQuery query = productInqueryPublicQuery()
                .principal(null)
                .userId(1L)
                .build();
        Post parentPost = buildProductInqueryPost(1L, 999L, targetId, true);
        Post replyPost = buildProductInqueryPost(2L, 999L, targetId, false);
        parentPost.addReplies(List.of(replyPost));
        mockPublicPosts(Posts.from(List.of(parentPost)));
        ProductInfoResult productInfo = buildProductInfo(777L);
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                .thenReturn(Map.of(targetId, productInfo));

        GetPostsResult result = getPostService.getPosts(query);

        assertThat(result.posts()).hasSize(1);
        PostItemResult item = result.posts().getFirst();
        assertThat(item.isMasked()).isTrue();
        assertThat(item.getReplies()).isNull();
    }

    // ========== J. 예외 전파 ==========

    @Test
    @DisplayName("findPublicPosts 실행 중 예외 발생 시 전파된다")
    void getPosts_findPublicPostsThrows_propagatesException() {
        GetPostsQuery query = publicQuery(Board.NOTICE).build();
        RuntimeException exception = new RuntimeException("database error");
        when(findPostPort.findPublicPosts(
                any(), any(), any(), any(), any(), any(), anyBoolean(), any(), any(), any(), any(), any(), any()
        )).thenThrow(exception);

        assertThatThrownBy(() -> getPostService.getPosts(query))
                .isSameAs(exception);
    }

    @Test
    @DisplayName("findUserPosts 실행 중 예외 발생 시 전파된다")
    void getPosts_findUserPostsThrows_propagatesException() {
        GetPostsQuery query = userQuery().build();
        RuntimeException exception = new RuntimeException("database error");
        when(findPostPort.findUserPosts(
                any(), any(), any(), any(), anyBoolean(), any(), any(), any(), any(), any()
        )).thenThrow(exception);

        assertThatThrownBy(() -> getPostService.getPosts(query))
                .isSameAs(exception);
    }

    // ========== K. 응답 분리 ==========

    @Test
    @DisplayName("상품 문의 목록 조회 시 응답에 maskedWriterName이 포함된다")
    void getPosts_productInquery_containsMaskedWriterName() {
        // given
        Long targetId = 100L;
        GetPostsQuery query = productInqueryPublicQuery().build();
        Post post = buildProductInqueryPostWithWriterName(1L, 1L, targetId, "작성자이름", "작*자");
        mockPublicPosts(Posts.from(List.of(post)));
        ProductInfoResult productInfo = buildProductInfo(1L);
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                .thenReturn(Map.of(targetId, productInfo));

        // when
        GetPostsResult result = getPostService.getPosts(query);

        // then
        assertThat(result.posts()).hasSize(1);
        assertThat(result.posts().getFirst().getMaskedWriterName()).isEqualTo("작*자");
    }

    @Test
    @DisplayName("상품 문의 목록 조회 시 응답에 writerName이 포함되지 않는다")
    void getPosts_productInquery_doesNotContainWriterName() {
        // given
        Long targetId = 100L;
        GetPostsQuery query = productInqueryPublicQuery().build();
        Post post = buildProductInqueryPostWithWriterName(1L, 1L, targetId, "작성자이름", "작*자");
        mockPublicPosts(Posts.from(List.of(post)));
        ProductInfoResult productInfo = buildProductInfo(1L);
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList()))
                .thenReturn(Map.of(targetId, productInfo));

        // when
        GetPostsResult result = getPostService.getPosts(query);

        // then
        assertThat(result.posts()).hasSize(1);
        // PostItemResult에는 writerName 필드가 존재하지 않음을 컴파일 타임에 보장
        // maskedWriterName만 포함되어 있는지 검증
        assertThat(result.posts().getFirst().getMaskedWriterName()).isNotNull();
        assertThat(result.posts().getFirst()).hasNoNullFieldsOrPropertiesExcept(
                "parentId", "productImageUrl", "images", "replies"
        );
    }

    // ========== Query Builders ==========

    private GetPostsQuery.GetPostsQueryBuilder publicQuery(Board board) {
        return GetPostsQuery.builder()
                .board(board)
                .userId(1L)
                .pageSize(10)
                .cursor(5L)
                .sortDirection(Sort.Direction.DESC);
    }

    private GetPostsQuery.GetPostsQueryBuilder userQuery() {
        return GetPostsQuery.builder()
                .board(Board.ONE_ON_ONE_INQUERY)
                .userId(1L)
                .pageSize(10)
                .cursor(5L)
                .sortDirection(Sort.Direction.DESC);
    }

    private GetPostsQuery.GetPostsQueryBuilder productInqueryPublicQuery() {
        return GetPostsQuery.builder()
                .board(Board.PRODUCT_INQUERY)
                .targetType(PostTargetType.PRICE_POLICY)
                .userId(1L)
                .pageSize(10)
                .cursor(5L)
                .sortDirection(Sort.Direction.DESC);
    }

    private GetPostsQuery.GetPostsQueryBuilder productInqueryUserQuery() {
        return GetPostsQuery.builder()
                .board(Board.PRODUCT_INQUERY)
                .targetType(null)
                .userId(1L)
                .pageSize(10)
                .cursor(5L)
                .sortDirection(Sort.Direction.DESC);
    }

    // ========== Post Builders ==========

    private Post buildNoticePost(Long id) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(1L)
                .board(Board.NOTICE)
                .category("ANNOUNCEMENT")
                .title("공지 제목")
                .content("공지 내용")
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildOneOnOnePost(Long id) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(1L)
                .board(Board.ONE_ON_ONE_INQUERY)
                .category("ORDER_PAYMENT")
                .title("문의 제목")
                .content("문의 내용")
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildOneOnOnePostWithContent(Long id, String title, String content) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(1L)
                .board(Board.ONE_ON_ONE_INQUERY)
                .category("ORDER_PAYMENT")
                .title(title)
                .content(content)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildProductInqueryPost(Long id, Long userId, Long targetId, boolean isPrivate) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(userId)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .targetType(PostTargetType.PRICE_POLICY)
                .targetId(targetId)
                .title("상품 문의 제목")
                .content("상품 문의 내용")
                .isPrivate(isPrivate)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildProductInqueryPostWithoutTarget(Long id) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(1L)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .title("상품 문의 제목")
                .content("상품 문의 내용")
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildProductInqueryPostWithContent(Long id, String title, String content) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(1L)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .title(title)
                .content(content)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildProductInqueryPostWithWriterName(Long id, Long userId, Long targetId, String writerName, String maskedWriterName) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(userId)
                .board(Board.PRODUCT_INQUERY)
                .category("PRODUCT_QUESTION")
                .targetType(PostTargetType.PRICE_POLICY)
                .targetId(targetId)
                .writerName(writerName)
                .maskedWriterName(maskedWriterName)
                .title("상품 문의 제목")
                .content("상품 문의 내용")
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private Post buildPhotoPost(Long id, Board board, String category) {
        return Post.from(PostSnapshotState.builder()
                .id(id)
                .userId(1L)
                .board(board)
                .category(category)
                .title("사진 게시글")
                .content("내용")
                .isPhoto(true)
                .status(EntityStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .orderNum(id)
                .build());
    }

    private ProductInfoResult buildProductInfo(Long sellerId) {
        return new ProductInfoResult(sellerId, "상품명", "브랜드명", null, List.of(), null);
    }

    // ========== Mock Helpers ==========

    private Posts emptyPosts() {
        return Posts.from(List.of());
    }

    private void mockPublicPosts(Posts posts) {
        when(findPostPort.findPublicPosts(
                any(), any(), any(), any(), any(), any(), anyBoolean(), any(), any(), any(), any(), any(), any()
        )).thenReturn(posts);
    }

    private void mockUserPosts(Posts posts) {
        when(findPostPort.findUserPosts(
                any(), any(), any(), any(), anyBoolean(), any(), any(), any(), any(), any()
        )).thenReturn(posts);
    }

    private void mockProductInfoPort(Map<Long, ProductInfoResult> infoMap) {
        when(findProductByPricePolicyPort.findByPricePolicyIds(anyList())).thenReturn(infoMap);
    }

    // ========== Capture Helpers ==========

    private Pageable capturePublicPostsPageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(findPostPort).findPublicPosts(
                any(), any(), any(), any(), any(), captor.capture(),
                anyBoolean(), any(), any(), any(), any(), any(), any()
        );
        return captor.getValue();
    }

    private Pageable captureUserPostsPageable() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(findPostPort).findUserPosts(
                any(), any(), any(), captor.capture(), anyBoolean(), any(), any(), any(), any(), any()
        );
        return captor.getValue();
    }

    // ========== Verify Helpers ==========

    private void verifyPublicPostsCalled() {
        verify(findPostPort).findPublicPosts(
                any(), any(), any(), any(), any(), any(), anyBoolean(), any(), any(), any(), any(), any(), any()
        );
    }

    private void verifyPublicPostsNeverCalled() {
        verify(findPostPort, never()).findPublicPosts(
                any(), any(), any(), any(), any(), any(), anyBoolean(), any(), any(), any(), any(), any(), any()
        );
    }

    private void verifyUserPostsCalled() {
        verify(findPostPort).findUserPosts(
                any(), any(), any(), any(), anyBoolean(), any(), any(), any(), any(), any()
        );
    }

    private void verifyUserPostsNeverCalled() {
        verify(findPostPort, never()).findUserPosts(
                any(), any(), any(), any(), anyBoolean(), any(), any(), any(), any(), any()
        );
    }
}
