package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.exception.PostNotFoundException;
import com.personal.marketnote.community.port.in.result.post.GetPostKeyResult;
import com.personal.marketnote.community.port.in.usecase.post.GetPostKeyUseCase;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetPostKeyService implements GetPostKeyUseCase {
    private final FindPostPort findPostPort;

    @Override
    public GetPostKeyResult getPostKey(Long postId, Long userId) {
        Post post = findPostPort.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        if (!post.isOwnedBy(userId)) {
            throw new PostNotFoundException(postId);
        }

        return GetPostKeyResult.from(post);
    }
}
