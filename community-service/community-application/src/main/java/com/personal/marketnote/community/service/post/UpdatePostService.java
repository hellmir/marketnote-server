package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.exception.InvalidPostContentContainsProfanityException;
import com.personal.marketnote.community.exception.PostNotEditableException;
import com.personal.marketnote.community.port.in.command.post.UpdatePostCommand;
import com.personal.marketnote.community.port.in.usecase.post.GetPostUseCase;
import com.personal.marketnote.community.port.in.usecase.post.UpdatePostUseCase;
import com.personal.marketnote.community.port.out.post.UpdatePostPort;
import com.personal.marketnote.community.port.out.profanity.FindProfanityWordPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class UpdatePostService implements UpdatePostUseCase {
    private final GetPostUseCase getPostUseCase;
    private final UpdatePostPort updatePostPort;
    private final FindProfanityWordPort findProfanityWordPort;

    @Override
    public void updatePost(UpdatePostCommand command) {
        Long id = command.id();
        Post post = getPostUseCase.getPost(id);

        if (!post.isEditable()) {
            throw new PostNotEditableException();
        }

        if (post.isProductInquiryPost() && !post.isReply()) {
            validateProfanity(command.title(), command.content());
        }

        post.update(command.title(), command.content());
        updatePostPort.update(post);
    }

    private void validateProfanity(String title, String content) {
        if (FormatValidator.hasValue(title) && findProfanityWordPort.containsProfanity(title)) {
            throw new InvalidPostContentContainsProfanityException();
        }
        if (FormatValidator.hasValue(content) && findProfanityWordPort.containsProfanity(content)) {
            throw new InvalidPostContentContainsProfanityException();
        }
    }
}
