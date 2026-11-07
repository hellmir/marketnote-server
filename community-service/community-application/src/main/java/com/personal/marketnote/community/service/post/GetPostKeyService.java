package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.exception.PostNotFoundException;
import com.personal.marketnote.community.port.in.result.post.GetPostKeyResult;
import com.personal.marketnote.community.port.in.usecase.post.GetPostKeyUseCase;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetPostKeyService implements GetPostKeyUseCase {
    private final FindPostPort findPostPort;

    @Override
    public GetPostKeyResult getPostKey(Long postId, Long userId) {
        Post post = findPostPort.findById(postId)
                .orElseThrow(() -> {
                    log.warn("게시글을 찾을 수 없습니다. postId={}", postId);
                    return new PostNotFoundException();
                });

        if (!post.isOwnedBy(userId)) {
            log.warn("게시글 소유자 불일치로 접근 거부. postId={}, userId={}", postId, userId);
            throw new PostNotFoundException();
        }

        return GetPostKeyResult.from(post);
    }
}
