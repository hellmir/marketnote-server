package com.personal.marketnote.file.port.out.storage;

import com.personal.marketnote.common.domain.file.OwnerType;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 파일 업로드 포트
 *
 * @Author 성효빈
 * @Date 2026-01-03
 * @Description 파일 업로드 기능을 제공합니다.
 */
public interface UploadFilesPort {
    /**
     * @param files     업로드할 파일 목록
     * @param ownerType 소유 도메인 타입
     * @param ownerId   소유 도메인 ID
     * @return 업로드된 파일 URL 목록
     * @Date 2026-01-03
     * @Author 성효빈
     * @Description 파일 목록을 업로드합니다.
     */
    List<String> uploadFiles(List<MultipartFile> files, OwnerType ownerType, Long ownerId);
}


