package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.PostTargetType;
import com.personal.marketnote.community.domain.post.Posts;
import com.personal.marketnote.community.port.in.command.post.GetUserProductInquiryPostsCommand;
import com.personal.marketnote.community.port.in.result.post.GetUserProductInquiryPostsResult;
import com.personal.marketnote.community.port.in.result.post.PostItemResult;
import com.personal.marketnote.community.port.in.result.post.PostProductInfoResult;
import com.personal.marketnote.community.port.in.usecase.post.GetUserProductInquiryPostsUseCase;
import com.personal.marketnote.community.port.out.file.FindPostImagesPort;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static com.personal.marketnote.common.domain.file.FileSort.POST_IMAGE;
import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetUserProductInquiryPostsService implements GetUserProductInquiryPostsUseCase {
    private final FindPostPort findPostPort;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;
    private final FindPostImagesPort findPostImagesPort;

    @Override
    public GetUserProductInquiryPostsResult getUserProductInquiryPosts(GetUserProductInquiryPostsCommand command) {
        boolean isDesc = Sort.Direction.DESC.equals(command.sortDirection());

        Posts posts = findPostPort.findUserPostsByOffset(
                command.userId(),
                Board.PRODUCT_INQUERY,
                command.page(),
                command.pageSize(),
                isDesc,
                command.sortProperty()
        );

        long totalElements = findPostPort.countUserPosts(
                command.userId(), Board.PRODUCT_INQUERY, null, null
        );

        int totalPages = computeTotalPages(totalElements, command.pageSize());

        List<Post> postList = posts.getPosts();

        Map<Long, ProductInfoResult> productInfoMap = getProductInfo(postList);

        Map<Long, List<GetFileResult>> postImagesByPostId = findPostImages(postList);

        List<PostItemResult> postItems = postList.stream()
                .map(post -> toPostItemResult(post, productInfoMap, postImagesByPostId))
                .toList();

        return GetUserProductInquiryPostsResult.of(
                command.page(),
                command.pageSize(),
                totalElements,
                totalPages,
                postItems
        );
    }

    private PostItemResult toPostItemResult(
            Post post,
            Map<Long, ProductInfoResult> productInfoMap,
            Map<Long, List<GetFileResult>> postImagesByPostId
    ) {
        List<GetFileResult> images = postImagesByPostId.get(post.getId());

        if (FormatValidator.hasNoValue(post.getTargetId())) {
            PostItemResult postItemResult = PostItemResult.from(post, images);
            if (post.hasReplies()) {
                postItemResult.addReplies(post, postImagesByPostId);
            }
            return postItemResult;
        }

        ProductInfoResult productInfoResult = productInfoMap.get(post.getTargetId());
        PostItemResult postItemResult = PostItemResult.from(
                post,
                PostProductInfoResult.from(productInfoResult),
                images
        );

        if (post.hasReplies()) {
            postItemResult.addReplies(post, postImagesByPostId);
        }

        return postItemResult;
    }

    private int computeTotalPages(long totalElements, int pageSize) {
        if (totalElements == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / pageSize);
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
