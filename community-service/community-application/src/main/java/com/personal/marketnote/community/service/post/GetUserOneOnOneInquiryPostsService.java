package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.application.file.port.in.result.GetFileResult;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.Board;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.domain.post.Posts;
import com.personal.marketnote.community.port.in.command.post.GetUserOneOnOneInquiryPostsCommand;
import com.personal.marketnote.community.port.in.result.post.GetUserOneOnOneInquiryPostsResult;
import com.personal.marketnote.community.port.in.result.post.PostItemResult;
import com.personal.marketnote.community.port.in.usecase.post.GetUserOneOnOneInquiryPostsUseCase;
import com.personal.marketnote.community.port.out.file.FindPostImagesPort;
import com.personal.marketnote.community.port.out.post.FindPostPort;
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
public class GetUserOneOnOneInquiryPostsService implements GetUserOneOnOneInquiryPostsUseCase {
    private final FindPostPort findPostPort;
    private final FindPostImagesPort findPostImagesPort;

    @Override
    public GetUserOneOnOneInquiryPostsResult getUserOneOnOneInquiryPosts(GetUserOneOnOneInquiryPostsCommand command) {
        boolean isDesc = Sort.Direction.DESC.equals(command.sortDirection());

        Posts posts = findPostPort.findUserPostsByOffset(
                command.userId(),
                Board.ONE_ON_ONE_INQUERY,
                command.page(),
                command.pageSize(),
                isDesc,
                command.sortProperty()
        );

        long totalElements = findPostPort.countUserPosts(
                command.userId(), Board.ONE_ON_ONE_INQUERY, null, null, null, null
        );

        int totalPages = computeTotalPages(totalElements, command.pageSize());

        List<Post> postList = posts.getPosts();

        Map<Long, List<GetFileResult>> postImagesByPostId = findPostImages(postList);

        List<PostItemResult> postItems = postList.stream()
                .map(post -> toPostItemResult(post, postImagesByPostId))
                .toList();

        return GetUserOneOnOneInquiryPostsResult.of(
                command.page(),
                command.pageSize(),
                totalElements,
                totalPages,
                postItems
        );
    }

    private PostItemResult toPostItemResult(
            Post post,
            Map<Long, List<GetFileResult>> postImagesByPostId
    ) {
        List<GetFileResult> images = postImagesByPostId.get(post.getId());

        PostItemResult postItemResult = PostItemResult.from(post, images);
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
