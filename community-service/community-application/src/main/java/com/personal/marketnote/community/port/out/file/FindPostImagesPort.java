package com.personal.marketnote.community.port.out.file;

import com.personal.marketnote.common.application.file.port.in.result.GetFilesResult;
import com.personal.marketnote.common.domain.file.FileSort;

import java.util.Optional;

/**
 * 게시글 이미지 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-17
 * @Description 게시글 이미지 조회 기능을 제공합니다.
 */
public interface FindPostImagesPort {
    /**
     * @param postId 게시글 ID
     * @param sort   파일 정렬 기준
     * @return 게시글 이미지 목록 {@link GetFilesResult}
     * @Date 2026-01-17
     * @Author 성효빈
     * @Description 게시글 ID와 정렬 기준으로 이미지를 조회합니다.
     */
    Optional<GetFilesResult> findImagesByPostIdAndSort(Long postId, FileSort sort);
}
