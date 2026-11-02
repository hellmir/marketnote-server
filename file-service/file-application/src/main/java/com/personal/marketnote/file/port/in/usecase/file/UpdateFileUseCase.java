package com.personal.marketnote.file.port.in.usecase.file;

import com.personal.marketnote.file.port.in.command.UpdateFilesCommand;

/**
 * 파일 수정 유스케이스
 *
 * @Author 성효빈
 * @Date 2026-02-05
 * @Description 파일 수정 기능을 제공합니다.
 */
public interface UpdateFileUseCase {
    /**
     * @param updateFilesCommand 파일 목록 수정 커맨드
     * @Date 2026-02-05
     * @Author 성효빈
     * @Description 파일 목록을 수정합니다.
     */
    void updateFiles(UpdateFilesCommand updateFilesCommand);
}