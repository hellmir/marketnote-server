package com.personal.marketnote.community.service.post;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.community.domain.post.NoticePostCategory;
import com.personal.marketnote.community.domain.post.Post;
import com.personal.marketnote.community.exception.InvalidPostContentContainsProfanityException;
import com.personal.marketnote.community.exception.NotProductSellerException;
import com.personal.marketnote.community.mapper.PostCommandToStateMapper;
import com.personal.marketnote.community.port.in.command.post.RegisterPostCommand;
import com.personal.marketnote.community.port.in.result.post.RegisterPostResult;
import com.personal.marketnote.community.port.in.usecase.post.RegisterPostUseCase;
import com.personal.marketnote.community.port.out.event.PublishPostEventPort;
import com.personal.marketnote.community.port.out.post.FindPostPort;
import com.personal.marketnote.community.port.out.post.SavePostPort;
import com.personal.marketnote.community.port.out.product.FindProductByPricePolicyPort;
import com.personal.marketnote.community.port.out.profanity.FindProfanityWordPort;
import com.personal.marketnote.community.port.out.result.product.ProductInfoResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@Slf4j
@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED)
public class RegisterPostService implements RegisterPostUseCase {

    private static final String DEFAULT_PRODUCT_INQUIRY_TITLE = "상품 문의";
    private static final int INQUIRY_TITLE_MAX_LENGTH = 200;

    private final SavePostPort savePostPort;
    private final FindPostPort findPostPort;
    private final FindProductByPricePolicyPort findProductByPricePolicyPort;
    private final FindProfanityWordPort findProfanityWordPort;
    private final PublishPostEventPort publishPostEventPort;

    @Override
    public RegisterPostResult registerPost(boolean isSeller, RegisterPostCommand command) {
        if (command.board().isProductInquery() && !command.isReply()) {
            validateProfanity(command.title(), command.content());
        }

        ProductInfoResult preFetchedProductInfo = null;
        Long preFetchedPricePolicyId = null;
        // 판매자의 상품 문의 답글인 경우 본인 판매 상품인지 여부 검증
        if (isSeller && command.isReply()) {
            preFetchedPricePolicyId = command.targetId();
            preFetchedProductInfo = findProductByPricePolicyPort
                    .findByPricePolicyIds(List.of(preFetchedPricePolicyId))
                    .get(preFetchedPricePolicyId);

            if (!isProductSeller(command.userId(), preFetchedProductInfo)) {
                throw new NotProductSellerException(preFetchedPricePolicyId);
            }
        }

        Post savedPost = savePostPort.save(
                Post.from(PostCommandToStateMapper.mapToState(command))
        );

        if (command.board().isNotice() && NoticePostCategory.ANNOUNCEMENT.isMe(command.category())) {
            publishPostEventPort.publishNoticeRegisteredEvent(
                    savedPost.getId(), command.title(), savedPost.isImportant()
            );
        }

        if (command.board().isNotice() && NoticePostCategory.EVENT.isMe(command.category())) {
            publishPostEventPort.publishEventRegisteredEvent(savedPost.getId(), command.title());
        }

        if (command.board().isOneOnOneInquery() && command.isReply()) {
            publishOneOnOneInquiryAnsweredEvent(command.parentId(), command.board().name());
        }

        if (command.board().isProductInquery() && command.isReply()) {
            publishProductInquiryAnsweredEvent(
                    command.parentId(), command.board().name(),
                    preFetchedProductInfo, preFetchedPricePolicyId
            );
        }

        return RegisterPostResult.from(savedPost);
    }

    private void publishOneOnOneInquiryAnsweredEvent(Long parentPostId, String board) {
        findPostPort.findById(parentPostId).ifPresent(parentPost -> {
            if (!parentPost.isOneOnOneInquiryPost()) {
                log.warn(
                        "1:1 문의 답변 이벤트 발행 skip - 부모 post board 불일치: parentPostId={}, parentBoard={}",
                        parentPostId, parentPost.getBoard()
                );
                return;
            }
            publishPostEventPort.publishInquiryAnsweredEvent(
                    parentPost.getUserId(), parentPost.getId(), parentPost.getTitle(), board
            );
        });
    }

    private void publishProductInquiryAnsweredEvent(
            Long parentPostId, String board,
            ProductInfoResult preFetchedProductInfo, Long preFetchedPricePolicyId
    ) {
        findPostPort.findById(parentPostId).ifPresent(parentPost -> {
            if (!parentPost.isProductInquiryPost()) {
                log.warn(
                        "상품 문의 답변 이벤트 발행 skip - 부모 post board 불일치: parentPostId={}, parentBoard={}",
                        parentPostId, parentPost.getBoard()
                );
                return;
            }
            String resolvedTitle = resolveProductInquiryTitle(
                    parentPost, preFetchedProductInfo, preFetchedPricePolicyId
            );
            publishPostEventPort.publishInquiryAnsweredEvent(
                    parentPost.getUserId(), parentPost.getId(), resolvedTitle, board
            );
        });
    }

    private String resolveProductInquiryTitle(
            Post parentPost, ProductInfoResult preFetchedProductInfo, Long preFetchedPricePolicyId
    ) {
        if (parentPost.hasNoTitle()) {
            return resolveTitleFromProduct(parentPost, preFetchedProductInfo, preFetchedPricePolicyId);
        }
        return parentPost.getTitle();
    }

    private String resolveTitleFromProduct(
            Post parentPost, ProductInfoResult preFetchedProductInfo, Long preFetchedPricePolicyId
    ) {
        if (!parentPost.isPricePolicyTarget()) {
            return DEFAULT_PRODUCT_INQUIRY_TITLE;
        }
        if (parentPost.hasNoTargetId()) {
            return DEFAULT_PRODUCT_INQUIRY_TITLE;
        }
        ProductInfoResult productInfo = resolveProductInfoForTitle(
                parentPost, preFetchedProductInfo, preFetchedPricePolicyId
        );
        if (FormatValidator.hasNoValue(productInfo)) {
            return DEFAULT_PRODUCT_INQUIRY_TITLE;
        }
        if (FormatValidator.hasNoValue(productInfo.name())) {
            return DEFAULT_PRODUCT_INQUIRY_TITLE;
        }
        return sanitizeProductNameForTitle(productInfo.name());
    }

    private ProductInfoResult resolveProductInfoForTitle(
            Post parentPost, ProductInfoResult preFetchedProductInfo, Long preFetchedPricePolicyId
    ) {
        if (FormatValidator.hasValue(preFetchedProductInfo)
                && parentPost.getTargetId().equals(preFetchedPricePolicyId)) {
            return preFetchedProductInfo;
        }
        return findProductByPricePolicyPort
                .findByPricePolicyIds(List.of(parentPost.getTargetId()))
                .get(parentPost.getTargetId());
    }

    // 판매자 입력 상품명을 이벤트 title로 주입하기 전 완화:
    // (1) TemplateRenderer 치환 후 검증이 치환 값 내부 {word} 패턴을 오탐하지 않도록 중괄호 제거
    // (2) notification.title VARCHAR(200) 컬럼 한도 초과로 저장 실패를 방지하기 위한 길이 제한
    // 근본 해결은 notification-service 중앙 방어선(#3773, #3774)에서 수행.
    private String sanitizeProductNameForTitle(String name) {
        String withoutBraces = name.replace("{", "").replace("}", "");
        if (withoutBraces.length() <= INQUIRY_TITLE_MAX_LENGTH) {
            return withoutBraces;
        }
        return withoutBraces.substring(0, INQUIRY_TITLE_MAX_LENGTH);
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
