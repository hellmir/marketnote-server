package com.personal.marketnote.product.port.out.file;

import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.file.FileSort;

import java.util.Optional;

/**
 * 상품 이미지 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-04
 * @Description 상품 이미지 조회 관련 기능을 제공합니다.
 */
public interface FindProductImagesPort {
    /**
     * @param productId 상품 ID
     * @param sort      파일 정렬 기준
     * @return 상품 이미지 목록 {@link Optional}&lt;{@link GetFilesResult}&gt;
     * @Date 2026-01-04
     * @Author 성효빈
     * @Description 상품 ID와 정렬 기준으로 상품 이미지를 조회합니다.
     */
    Optional<GetFilesResult> findImagesByProductIdAndSort(Long productId, FileSort sort);
}
