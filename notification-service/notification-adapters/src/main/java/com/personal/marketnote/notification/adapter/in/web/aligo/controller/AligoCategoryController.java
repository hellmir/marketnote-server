package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.GetAligoCategoryResponse;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoCategoryTreeResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoCategoryUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@Tag(name = "(관리자) 알리고 카테고리", description = "알리고 카카오채널 카테고리 조회 API")
@SecurityRequirement(name = "bearer")
@RestController
@RequestMapping("/api/v1/admin/aligo/categories")
@RequiredArgsConstructor
public class AligoCategoryController {

    private final GetAligoCategoryUseCase getAligoCategoryUseCase;

    @GetMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @Operation(summary = "카카오채널 카테고리 목록 조회", description = "알리고 카카오채널 카테고리 트리를 조회합니다.")
    public ResponseEntity<BaseResponse<GetAligoCategoryResponse>> getCategoryTree() {
        GetAligoCategoryTreeResult result = getAligoCategoryUseCase.getCategoryTree();

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetAligoCategoryResponse.from(result),
                        HttpStatus.OK, DEFAULT_SUCCESS_CODE, "카테고리 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
