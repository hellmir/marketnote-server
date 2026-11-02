package com.personal.marketnote.file.port.in.usecase.file;

/**
 * 파일 삭제 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-07
 * @Description 파일 삭제 기능을 제공합니다.
 */
public interface DeleteFileUseCase {
    /**
     * @param id 파일 ID
     * @Date 2026-01-07
     * @Author 성효빈
     * @Description 파일을 삭제합니다.
     */
    void delete(Long id);
}


