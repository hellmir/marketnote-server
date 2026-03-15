package com.personal.marketnote.common.adapter.in.web.kafka.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.in.web.kafka.request.ReprocessDltRequest;
import com.personal.marketnote.common.adapter.in.web.kafka.response.ReprocessDltResponse;
import com.personal.marketnote.common.configuration.kafka.DltReprocessResult;
import com.personal.marketnote.common.configuration.kafka.DltReprocessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@RestController
@Tag(name = "관리자 DLT API", description = "Dead Letter Topic 관리 및 재처리")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class AdminDltController {
    private final DltReprocessService dltReprocessService;

    @PostMapping("/api/v1/admin/kafka/dlt/reprocess")
    @PreAuthorize(ADMIN_POINTCUT)
    @SecurityRequirement(name = "bearer")
    @Operation(summary = "DLT 메시지 수동 재처리", description = "지정된 원본 토픽의 DLT 메시지를 원본 토픽으로 재전송합니다.")
    public ResponseEntity<BaseResponse<ReprocessDltResponse>> reprocessDlt(
            @Valid @RequestBody ReprocessDltRequest request
    ) {
        DltReprocessResult result = dltReprocessService.reprocess(request.originalTopic());

        return new ResponseEntity<>(
                BaseResponse.of(
                        ReprocessDltResponse.from(request.originalTopic(), result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "DLT 메시지 재처리 완료"
                ),
                HttpStatus.OK
        );
    }
}
