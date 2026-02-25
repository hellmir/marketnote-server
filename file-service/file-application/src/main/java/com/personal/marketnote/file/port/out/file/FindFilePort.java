package com.personal.marketnote.file.port.out.file;

import com.personal.marketnote.common.domain.file.FileSort;
import com.personal.marketnote.common.domain.file.OwnerType;
import com.personal.marketnote.file.domain.file.FileDomain;

import java.util.List;
import java.util.Optional;

/**
 * 파일 조회 포트
 *
 * @Author 성효빈
 * @Date 2026-01-03
 * @Description 파일 조회 관련 기능을 제공합니다.
 */
public interface FindFilePort {
    /**
     * @param id 파일 ID
     * @return 파일 도메인 {@link FileDomain}
     * @Date 2026-01-03
     * @Author 성효빈
     * @Description ID로 파일을 조회합니다.
     */
    Optional<FileDomain> findById(Long id);

    /**
     * @param ownerType 소유 도메인 타입
     * @param ownerId   소유 도메인 ID
     * @return 파일 도메인 목록 {@link FileDomain}
     * @Date 2026-01-03
     * @Author 성효빈
     * @Description 소유자 기준으로 파일 목록을 조회합니다.
     */
    List<FileDomain> findByOwner(OwnerType ownerType, Long ownerId);

    /**
     * @param ownerType 소유 도메인 타입
     * @param ownerId   소유 도메인 ID
     * @param sort      파일 종류
     * @return 파일 도메인 목록 {@link FileDomain}
     * @Date 2026-01-03
     * @Author 성효빈
     * @Description 소유자 및 파일 종류 기준으로 파일 목록을 조회합니다.
     */
    List<FileDomain> findByOwnerAndSort(OwnerType ownerType, Long ownerId, FileSort sort);
}


