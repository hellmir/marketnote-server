package com.personal.marketnote.file.port.out.resized;

import com.personal.marketnote.file.domain.file.ResizedFile;

import java.util.List;

/**
 * 리사이즈 파일 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-03
 * @Description 리사이즈 파일 저장 기능을 제공합니다.
 */
public interface SaveResizedFilesPort {
    /**
     * @param resizedFiles 리사이즈 파일 목록
     * @Date 2026-01-03
     * @Author 성효빈
     * @Description 리사이즈 파일 목록을 저장합니다.
     */
    void saveAll(List<ResizedFile> resizedFiles);
}


