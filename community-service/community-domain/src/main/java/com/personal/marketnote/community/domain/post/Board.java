package com.personal.marketnote.community.domain.post;

import com.personal.marketnote.common.utility.FormatConverter;
import com.personal.marketnote.community.domain.post.exception.InvalidPostCategoryException;
import com.personal.marketnote.community.domain.post.exception.InvalidPostCategoryForBoardException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@Getter
public enum Board {
    NOTICE("공지"),
    FAQ("FAQ"),
    PRODUCT_INQUERY("상품 문의"),
    ONE_ON_ONE_INQUERY("1:1 문의");

    private final String description;
    private final String camelCaseValue;

    Board(String description) {
        this.description = description;
        camelCaseValue = FormatConverter.snakeToCamel(name());
    }

    public PostCategory resolveCategory(String categoryCode) {
        if (this == NOTICE) {
            return Arrays.stream(NoticePostCategory.values())
                    .filter(category -> category.isMe(categoryCode))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.warn("게시판에 맞지 않는 카테고리입니다. board={}, categoryCode={}", this.name(), categoryCode);
                        return new InvalidPostCategoryForBoardException();
                    });
        }

        if (this == FAQ) {
            return Arrays.stream(FaqPostCategory.values())
                    .filter(category -> category.isMe(categoryCode))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.warn("게시판에 맞지 않는 카테고리입니다. board={}, categoryCode={}", this.name(), categoryCode);
                        return new InvalidPostCategoryForBoardException();
                    });
        }

        if (this == PRODUCT_INQUERY) {
            return Arrays.stream(ProductInqueryPostCategory.values())
                    .filter(category -> category.isMe(categoryCode))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.warn("게시판에 맞지 않는 카테고리입니다. board={}, categoryCode={}", this.name(), categoryCode);
                        return new InvalidPostCategoryForBoardException();
                    });
        }

        if (this == ONE_ON_ONE_INQUERY) {
            return Arrays.stream(OneOnOneInqueryPostCategory.values())
                    .filter(category -> category.isMe(categoryCode))
                    .findFirst()
                    .orElseThrow(() -> {
                        log.warn("게시판에 맞지 않는 카테고리입니다. board={}, categoryCode={}", this.name(), categoryCode);
                        return new InvalidPostCategoryForBoardException();
                    });
        }

        log.warn("유효하지 않은 카테고리명입니다. board={}, categoryCode={}", this.name(), categoryCode);
        throw new InvalidPostCategoryException();
    }

    public boolean isAdminRequired() {
        return this == NOTICE || this == FAQ;
    }

    public boolean isNotice() {
        return this == NOTICE;
    }

    public boolean isFaq() {
        return this == FAQ;
    }

    public boolean isProductInquery() {
        return this == PRODUCT_INQUERY;
    }

    public boolean isOneOnOneInquery() {
        return this == ONE_ON_ONE_INQUERY;
    }

    public boolean isNonMemberViewBoard() {
        return this == NOTICE || this == FAQ;
    }

    public boolean isEditable() {
        return isNotice() || isFaq();
    }

    public boolean requiresWriterMasking() {
        return !isOneOnOneInquery();
    }
}
