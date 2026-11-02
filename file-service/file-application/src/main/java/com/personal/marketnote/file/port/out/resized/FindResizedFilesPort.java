package com.personal.marketnote.file.port.out.resized;

import com.personal.marketnote.file.domain.file.ResizedFile;

import java.util.List;

/**
 * 리사이즈 파일 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-03
 * @Description 리사이즈 파일 조회 기능을 제공합니다.
 */
public interface FindResizedFilesPort {
    /**
     * @param fileIds 파일 ID 목록
     * @return 리사이즈 파일 목록 {@link ResizedFile}
     * @Date 2026-01-03
     * @Author 성효빈
     * @Description 파일 ID 목록으로 리사이즈 파일을 조회합니다.
     */
    List<ResizedFile> findByFileIds(List<Long> fileIds);
}


