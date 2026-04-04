package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.exception.InvalidPostContentContainsProfanityException;
import com.personal.marketnote.community.exception.NotProductSellerException;
import com.personal.marketnote.community.mapper.PostCommandToStateMapper;
import com.personal.marketnote.community.port.in.command.post.RegisterPostCommand;
import com.personal.marketnote.community.port.in.result.post.RegisterPostResult;
import com.personal.marketnote.community.port.in.usecase.post.RegisterPostUseCase;
import com.personal.marketnote.community.port.out.post.SavePostPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.profanity.FindProfanityWordPort;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterPostService implements RegisterPostUseCase {
    private final SavePostPort savePostPort;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;
    private final FindProfanityWordPort findProfanityWordPort;

    @Override
    public RegisterPostResult registerPost(boolean isSeller, RegisterPostCommand command) {
        if (command.board().isProductInquery() && !command.isReply()) {
            validateProfanity(command.title(), command.content());
        }

        // 판매자의 상품 문의 답글인 경우 본인 판매 상품인지 여부 검증
        if (isSeller && command.isReply()) {
            Long pricePolicyId = command.targetId();
            ProductInfoResult productInfoResult
                    = findProductByPricePolicyPort.findByPricePolicyIds(List.of(pricePolicyId))
                    .get(pricePolicyId);

            if (!isProductSeller(command.userId(), productInfoResult)) {
                throw new NotProductSellerException(pricePolicyId);
            }
        }

        Post savedPost = savePostPort.save(
                Post.from(PostCommandToStateMapper.mapToState(command))
        );

        return RegisterPostResult.from(savedPost);
    }

    private boolean isProductSeller(Long userId, ProductInfoResult productInfoResult) {
        return FormatValidator.hasValue(productInfoResult) && productInfoResult.isMyProduct(userId);
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
