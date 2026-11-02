package com.personal.marketnote.file.port.out.file;

import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.ResizedFile;

import java.util.List;

/**
 * 파일 목록 수정 포트
 *
 * @Author 성효빈
 * @Date 2026-02-05
 * @Description 파일 목록 수정 기능을 제공합니다.
 */
public interface UpdateFilesPort {
    /**
     * @param files        파일 도메인 목록
     * @param resizedFiles 리사이즈 파일 목록
     * @Date 2026-02-05
     * @Author 성효빈
     * @Description 파일 목록과 리사이즈 파일을 수정합니다.
     */
    void update(List<FileDomain> files, List<ResizedFile> resizedFiles);
}


