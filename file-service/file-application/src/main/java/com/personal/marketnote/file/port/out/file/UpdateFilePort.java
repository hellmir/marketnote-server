package com.personal.marketnote.file.port.out.file;

import com.personal.marketnote.file.domain.file.FileDomain;

/**
 * 파일 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-01-07
 * @Description 파일 수정 기능을 제공합니다.
 */
public interface UpdateFilePort {
    /**
     * @param file 파일 도메인
     * @Date 2026-01-07
     * @Author 성효빈
     * @Description 파일을 수정합니다.
     */
    void update(FileDomain file);
}
