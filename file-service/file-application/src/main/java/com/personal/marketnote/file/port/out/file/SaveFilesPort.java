package com.personal.marketnote.file.port.out.file;

import com.personal.marketnote.file.domain.file.FileDomain;

import java.util.List;

/**
 * 파일 저장 포트
 *
 * @Author 성효빈
 * @Date 2026-01-03
 * @Description 파일 저장 기능을 제공합니다.
 */
public interface SaveFilesPort {
    /**
     * @param files       파일 도메인 목록
     * @param storageUrls 스토리지 업로드 URL 목록
     * @return 저장된 파일 도메인 목록 {@link FileDomain}
     * @Date 2026-01-03
     * @Author 성효빈
     * @Description 파일 목록을 저장합니다.
     */
    List<FileDomain> saveAll(List<FileDomain> files, List<String> storageUrls);
}


