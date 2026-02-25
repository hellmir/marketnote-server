package com.personal.marketnote.file.port.in.usecase.file;

import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;
import com.personal.marketnote.file.domain.file.ResizedFile;
import com.personal.marketnote.file.port.in.result.GetFilesResult;

import java.util.List;

/**
 * 파일 조회 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-01-03
 * @Description 파일 조회 관련 기능을 제공합니다.
 */
public interface GetFileUseCase {
    /**
     * @param id 파일 ID
     * @return 파일 도메인 {@link FileDomain}
     * @Date 2026-01-03
     * @Author 성효빈
     * @Description 파일을 조회합니다.
     */
    FileDomain getFile(Long id);

    /**
     * @param ownerType 소유 도메인 타입
     * @param ownerId   소유 도메인 ID
     * @param sort      파일 종류
     * @return 파일 목록 조회 결과 {@link GetFilesResult}
     * @Date 2026-01-03
     * @Author 성효빈
     * @Description 파일 목록을 조회합니다.
     */
    GetFilesResult getFiles(String ownerType, Long ownerId, String sort);

    /**
     * @param ownerType 소유 도메인 타입
     * @param ownerId   소유 도메인 ID
     * @param sort      파일 종류
     * @return 파일 도메인 목록 {@link FileDomain}
     * @Date 2026-01-03
     * @Author 성효빈
     * @Description 파일 도메인 목록을 조회합니다.
     */
    List<FileDomain> getFiles(OwnerType ownerType, Long ownerId, String sort);

    /**
     * @param files 파일 도메인 목록
     * @return 리사이즈 파일 목록 {@link ResizedFile}
     * @Date 2026-01-03
     * @Author 성효빈
     * @Description 리사이즈된 파일 목록을 조회합니다.
     */
    List<ResizedFile> getResizedFiles(List<FileDomain> files);
}
