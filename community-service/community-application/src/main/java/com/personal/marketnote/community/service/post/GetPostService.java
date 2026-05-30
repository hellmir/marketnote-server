package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.AuthorityValidator;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.*;
import com.personal.marketnote.community.exception.PostBoardMismatchException;
import com.personal.marketnote.community.exception.PostNotFoundException;
import com.personal.marketnote.community.port.in.command.post.GetPostQuery;
import com.personal.marketnote.community.port.in.command.post.GetPostsQuery;
import com.personal.marketnote.community.port.in.result.post.GetPostsResult;
import com.personal.marketnote.community.port.in.result.post.PostItemResult;
import com.personal.marketnote.community.port.in.result.post.PostProductInfoResult;
import com.personal.marketnote.community.port.in.usecase.post.GetPostUseCase;
import com.personal.marketnote.community.port.out.file.FindPostImagesPort;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.personal.marketnote.common.domain.file.FileSort.POST_IMAGE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetPostService implements GetPostUseCase {
    private final FindPostPort findPostPort;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;
    private final FindPostImagesPort findPostImagesPort;

    @Override
    public boolean existsPost(Long id) {
        return findPostPort.existsById(id);
    }

    @Override
    public GetPostsResult getPosts(GetPostsQuery query) {
        Sort.Direction sortDirection = FormatValidator.hasValue(query.sortDirection())
                ? query.sortDirection()
                : Sort.Direction.DESC;
        PostSortProperty sortProperty = resolveSortProperty(query);
        Pageable pageable = PageRequest.of(0, query.pageSize() + 1, Sort.by(sortDirection, sortProperty.getSortField()));

        Posts posts = getBoardPosts(query, pageable, sortProperty);

        return generatePage(query, posts);
    }

    private Posts getBoardPosts(GetPostsQuery query, Pageable pageable, PostSortProperty sortProperty) {
        Board board = query.board();
        boolean isDesc = Sort.Direction.DESC.equals(query.sortDirection());
        Long userId = query.userId();

        // 비회원 전용 게시판이거나 상품 상세 정보의 문의 게시판인 경우
        if (query.isPublicPosts()) {
            return findPostPort.findPublicPosts(
                    board,
                    query.category(),
                    query.targetGroupType(),
                    query.targetGroupId(),
                    query.cursor(),
                    pageable,
                    isDesc,
                    sortProperty,
                    userId,
                    query.filter(),
                    query.filterValue(),
                    query.searchTarget(),
                    query.searchKeyword()
            );
        }

        // 나의 상품 문의 게시판이거나 나의 1:1 문의 게시판인 경우
        return findPostPort.findUserPosts(
                userId,
                board,
                query.cursor(),
                pageable,
                isDesc,
                sortProperty,
                query.filter(),
                query.filterValue(),
                query.searchTarget(),
                query.searchKeyword()
        );
    }

    private GetPostsResult generatePage(GetPostsQuery query, Posts posts) {
        Long totalElements = null;
        PostTargetType targetType = query.targetType();
        Board board = query.board();
        if (FormatValidator.hasNoValue(query.cursor())) {
            totalElements = computeTotalPageCount(query);
        }

        // 상품 문의 게시판인 경우 각 게시글의 주문 상품 정보 조회
        Map<Long, ProductInfoResult> targetProductsByPricePolicyId = board.isProductInquery()
                ? getProductInfo(posts.getPosts())
                : Map.of();

        List<Post> filteredPosts = posts.getPosts();

        // 나의 상품 문의 게시판 또는 1:1 문의 게시판인 경우 검색어 적용
        if (board.isOneOnOneInquery() || board.isProductInquery() && FormatValidator.hasNoValue(targetType)) {
            if (FormatValidator.hasValue(query.searchKeyword())) {
                PostSearchTarget keywordCategory = query.searchTarget();
                String searchKeyword = query.searchKeyword().toLowerCase();
                filteredPosts = filteredPosts.stream()
                        .filter(post -> matchesKeyword(post, keywordCategory, searchKeyword))
                        .toList();
            }
        }

        boolean hasNext = filteredPosts.size() > query.pageSize();
        List<Post> pagedPosts = hasNext
                ? filteredPosts.subList(0, query.pageSize())
                : filteredPosts;

        Long nextCursor = null;
        if (FormatValidator.hasValue(pagedPosts)) {
            nextCursor = pagedPosts.getLast().getId();
        }

        Map<Long, List<GetFileResult>> postImagesByPostId = findPostImages(pagedPosts);

        List<PostItemResult> postItems = pagedPosts.stream()
                .map(post -> {
                    List<GetFileResult> images = postImagesByPostId.get(post.getId());
                    if (FormatValidator.hasNoValue(post.getTargetId())) {
                        return PostItemResult.from(post, images);
                    }

                    // 상품 문의 게시판인 경우 각 게시글의 상품 정보 포함
                    ProductInfoResult productInfoResult = targetProductsByPricePolicyId.get(post.getTargetId());
                    PostItemResult postItemResult = PostItemResult.from(
                            post,
                            PostProductInfoResult.from(productInfoResult),
                            images
                    );

                    // 상품 상세 정보 페이지의 상품 문의 게시판인 경우 비밀글 필터링
                    if (FormatValidator.hasValue(targetType)) {
                        Long sellerId = FormatValidator.hasValue(productInfoResult)
                                ? productInfoResult.sellerId()
                                : null;
                        if (!adminOrSeller(query.principal(), query.userId(), sellerId)) {
                            postItemResult.maskPrivatePost(query.userId());
                        }
                    }

                    // 게시글의 답글 목록 추가
                    if (!postItemResult.isMasked() && post.hasReplies()) {
                        postItemResult.addReplies(post, postImagesByPostId);
                    }

                    return postItemResult;
                })
                .toList();

        return GetPostsResult.of(hasNext, nextCursor, totalElements, postItems);
    }

    private long computeTotalPageCount(GetPostsQuery query) {
        Long userId = query.userId();
        Board board = query.board();
        PostSearchTarget searchTarget = query.searchTarget();
        String searchKeyword = query.searchKeyword();

        if (query.isPublicPosts()) {
            return findPostPort.countPublicPosts(
                    board,
                    query.category(),
                    query.targetGroupType(),
                    query.targetGroupId(),
                    userId,
                    query.filter(),
                    query.filterValue(),
                    searchTarget,
                    searchKeyword
            );
        }

        return findPostPort.countUserPosts(
                userId,
                board,
                query.filter(),
                query.filterValue(),
                searchTarget,
                searchKeyword
        );
    }

    @Override
    public PostItemResult getPost(GetPostQuery query) {
        Post post = getPostWithReplies(query.id());
        validateQueryMatchesPost(query, post);
        Map<Long, List<GetFileResult>> postImagesByPostId = findPostImages(List.of(post));
        List<GetFileResult> images = postImagesByPostId.get(post.getId());

        if (FormatValidator.hasNoValue(post.getTargetId())) {
            PostItemResult postItemResult = PostItemResult.from(post, images);
            postItemResult.addReplies(post, postImagesByPostId);

            return postItemResult;
        }

        ProductInfoResult productInfoResult = null;
        if (query.board().isProductInquery()
                && PostTargetType.PRICE_POLICY.equals(post.getTargetType())
                && FormatValidator.hasValue(post.getTargetId())) {
            productInfoResult = findProductByPricePolicyPort.findByPricePolicyIds(List.of(post.getTargetId()))
                    .get(post.getTargetId());
        }

        PostItemResult postItemResult = PostItemResult.from(
                post,
                PostProductInfoResult.from(productInfoResult),
                images
        );

        if (FormatValidator.hasValue(query.targetType())) {
            Long sellerId = FormatValidator.hasValue(productInfoResult)
                    ? productInfoResult.sellerId()
                    : null;
            if (!adminOrSeller(query.principal(), query.userId(), sellerId)) {
                postItemResult.maskPrivatePost(query.userId());
            }
        }

        if (!postItemResult.isMasked() && post.hasReplies()) {
            postItemResult.addReplies(post, postImagesByPostId);
        }

        return postItemResult;
    }

    private Post getPostWithReplies(Long id) {
        return findPostPort.findByIdWithReplies(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    private void validateQueryMatchesPost(GetPostQuery query, Post post) {
        if (!Objects.equals(query.board(), post.getBoard())) {
            throw new PostBoardMismatchException();
        }
    }

    private boolean adminOrSeller(OAuth2AuthenticatedPrincipal principal, Long userId, Long sellerId) {
        return AuthorityValidator.hasAdminRole(principal)
                || (FormatValidator.equals(userId, sellerId) && AuthorityValidator.hasSellerRole(principal));
    }

    private Map<Long, ProductInfoResult> getProductInfo(List<Post> posts) {
        List<Long> pricePolicyIds = posts.stream()
                .filter(post -> PostTargetType.PRICE_POLICY.equals(post.getTargetType()))
                .map(Post::getTargetId)
                .filter(FormatValidator::hasValue)
                .distinct()
                .toList();

        return findProductByPricePolicyPort.findByPricePolicyIds(pricePolicyIds);
    }

    private PostSortProperty resolveSortProperty(GetPostsQuery query) {
        if (query.board().isNonMemberViewBoard()) {
            return PostSortProperty.ORDER_NUM;
        }

        return FormatValidator.hasValue(query.sortProperty()) ? query.sortProperty() : PostSortProperty.ID;
    }

    private boolean matchesKeyword(Post post, PostSearchTarget searchTarget, String searchKeyword) {
        if (searchTarget == PostSearchTarget.TITLE) {
            return containsKeyword(post.getTitle(), searchKeyword);
        }

        if (searchTarget == PostSearchTarget.CONTENT) {
            return containsKeyword(post.getContent(), searchKeyword);
        }

        return containsKeyword(post.getTitle(), searchKeyword) || containsKeyword(post.getContent(), searchKeyword);
    }

    private boolean containsKeyword(String target, String keyword) {
        if (FormatValidator.hasNoValue(target)) {
            return false;
        }

        return target.toLowerCase().contains(keyword);
    }

    @Override
    public Post getPost(Long id) {
        return findPostPort.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    private Map<Long, List<GetFileResult>> findPostImages(List<Post> posts) {
        if (FormatValidator.hasNoValue(posts)) {
            return Map.of();
        }

        Map<Long, List<GetFileResult>> postImages = new ConcurrentHashMap<>();

        List<Post> photoPosts = posts.stream()
                .flatMap(post -> {
                    Stream<Post> replies = post.hasReplies() ? post.getReplies().stream() : Stream.empty();
                    return Stream.concat(Stream.of(post), replies);
                })
                .filter(Post::isPhoto)
                .toList();

        List<CompletableFuture<Void>> futures = photoPosts.stream()
                .map(post -> CompletableFuture.runAsync(
                        () -> findPostImagesPort.findImagesByPostIdAndSort(post.getId(), POST_IMAGE)
                                .ifPresent(result -> postImages.put(post.getId(), result.images()))
                ))
                .toList();

        if (FormatValidator.hasValue(futures)) {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        }

        return postImages;
    }
}
