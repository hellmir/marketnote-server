package com.personal.marketnote.common.domain.file;

import com.personal.marketnote.common.utility.FormatValidator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum FileSort {
    PRODUCT_CATALOG_IMAGE("상품 카탈로그 이미지", 1),
    PRODUCT_REPRESENTATIVE_IMAGE("상품 대표 이미지", 8),
    PRODUCT_CONTENT_IMAGE("상품 본문 이미지", 5),
    CANCEL_REASON_IMAGE("취소 사유 이미지", Integer.MAX_VALUE),
    EXCHANGE_REASON_IMAGE("교환 사유 이미지", Integer.MAX_VALUE),
    REFUND_REASON_IMAGE("환불 사유 이미지", Integer.MAX_VALUE),
    POST_IMAGE("게시글 이미지", Integer.MAX_VALUE),
    REVIEW_IMAGE("리뷰 이미지", Integer.MAX_VALUE),
    ICON("아이콘", Integer.MAX_VALUE),
    ETC("기타", Integer.MAX_VALUE);

    private final String description;
    private final int maxCount;

    public static FileSort from(String targetValue) throws IllegalArgumentException {
        return Arrays.stream(FileSort.values())
                .filter(fileSort -> FormatValidator.equals(fileSort.name(), targetValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid file sort: " + targetValue));
    }

    public boolean isCatalogImage() {
        return this == FileSort.PRODUCT_CATALOG_IMAGE;
    }

    public boolean isRepresentativeImage() {
        return this == FileSort.PRODUCT_REPRESENTATIVE_IMAGE;
    }
}
