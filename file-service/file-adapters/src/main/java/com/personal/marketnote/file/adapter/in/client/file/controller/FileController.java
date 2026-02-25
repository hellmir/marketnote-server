package com.personal.marketnote.file.adapter.in.client.file.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.file.adapter.in.client.file.controller.apidocs.DeleteFileApiDocs;
import com.personal.marketnote.file.adapter.in.client.file.controller.apidocs.GetFilesApiDocs;
import com.personal.marketnote.file.adapter.in.client.file.controller.apidocs.UpdateFilesApiDocs;
import com.personal.marketnote.file.adapter.in.client.file.mapper.FileRequestToCommandMapper;
import com.personal.marketnote.file.adapter.in.client.file.request.UpdateFilesRequest;
import com.personal.marketnote.file.port.in.result.GetFilesResult;
import com.personal.marketnote.file.port.in.usecase.file.DeleteFileUseCase;
import com.personal.marketnote.file.port.in.usecase.file.GetFileUseCase;
import com.personal.marketnote.file.port.in.usecase.file.UpdateFileUseCase;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

/**
 * 파일 컨트롤러
 *
 * @Author 성효빈
 * @Date 2026-01-03
 * @Description 파일 관련 API를 제공합니다.
 */
@Controller
@RequestMapping("/api/v1/files")
@Tag(name = "파일 API", description = "파일 관련 API")
@RequiredArgsConstructor
public class FileController {
    private final UpdateFileUseCase updateFileUseCase;
    private final GetFileUseCase getFileUseCase;
    private final DeleteFileUseCase deleteFileUseCase;

    /**
     * 파일 목록 업데이트
     *
     * @param updateFilesRequest 파일 목록 업데이트 요청
     * @Author 성효빈
     * @Date 2026-01-03
     * @Description 요청 리소스의 파일 목록을 업데이트합니다. 기존 파일 목록은 비활성화됩니다.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @UpdateFilesApiDocs
    public ResponseEntity<BaseResponse<Void>> updateFiles(
            @Parameter(hidden = true) @ModelAttribute UpdateFilesRequest updateFilesRequest
    ) {
        updateFileUseCase.updateFiles(FileRequestToCommandMapper.mapToCommand(updateFilesRequest));

        return new ResponseEntity<>(
                BaseResponse.of(
                        null,
                        HttpStatus.CREATED,
                        DEFAULT_SUCCESS_CODE,
                        "파일 추가 성공"
                ),
                HttpStatus.CREATED
        );
    }

    /**
     * 파일 목록 조회
     *
     * @param ownerType 소유 도메인 타입
     * @param ownerId   소유 도메인 ID
     * @param sort      파일 종류
     * @Author 성효빈
     * @Date 2026-01-07
     * @Description 파일 목록을 조회합니다.
     */
    @GetMapping
    @GetFilesApiDocs
    public ResponseEntity<BaseResponse<GetFilesResult>> getFiles(
            @RequestParam("ownerType") String ownerType,
            @RequestParam("ownerId") Long ownerId,
            @RequestParam(value = "sort", required = false) String sort
    ) {
        GetFilesResult result = getFileUseCase.getFiles(ownerType, ownerId, sort);

        return new ResponseEntity<>(
                BaseResponse.of(
                        result,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파일 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    /**
     * 파일 삭제
     *
     * @param id 파일 ID
     * @Author 성효빈
     * @Date 2026-01-07
     * @Description 파일을 삭제합니다.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize(ADMIN_POINTCUT)
    @DeleteFileApiDocs
    public ResponseEntity<BaseResponse<Void>> deleteFile(@PathVariable("id") Long id) {
        deleteFileUseCase.delete(id);

        return new ResponseEntity<>(
                BaseResponse.of(
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "파일 삭제 성공"
                ),
                HttpStatus.OK
        );
    }
}
