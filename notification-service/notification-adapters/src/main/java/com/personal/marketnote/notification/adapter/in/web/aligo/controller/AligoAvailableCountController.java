package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.GetAligoAvailableCountResponse;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoAvailableCountResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoAvailableCountUseCase;
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

@Tag(name = "(관리자) 알리고 발송가능건수", description = "알리고 발송가능건수 조회 API")
@SecurityRequirement(name = "bearer")
@RestController
@RequestMapping("/api/v1/admin/aligo/available-counts")
@RequiredArgsConstructor
public class AligoAvailableCountController {

    private final GetAligoAvailableCountUseCase getAligoAvailableCountUseCase;

    @GetMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @Operation(summary = "발송가능건수 조회", description = "알리고 잔여 포인트 기반 SMS/LMS/MMS/ALT 발송가능건수를 조회한다.")
    public ResponseEntity<BaseResponse<GetAligoAvailableCountResponse>> getAvailableCount() {
        GetAligoAvailableCountResult result = getAligoAvailableCountUseCase.getAvailableCount();

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetAligoAvailableCountResponse.from(result),
                        HttpStatus.OK, DEFAULT_SUCCESS_CODE, "발송가능건수 조회 성공"
                ),
                HttpStatus.OK
        );
    }
}
