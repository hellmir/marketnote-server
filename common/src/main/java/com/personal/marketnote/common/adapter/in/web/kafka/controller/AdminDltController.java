package com.personal.marketnote.common.adapter.in.web.kafka.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.adapter.in.web.kafka.controller.apidocs.QueryDltMessagesApiDocs;
import com.personal.marketnote.common.adapter.in.web.kafka.controller.apidocs.QueryDltTopicSummariesApiDocs;
import com.personal.marketnote.common.adapter.in.web.kafka.controller.apidocs.ReprocessDltApiDocs;
import com.personal.marketnote.common.adapter.in.web.kafka.controller.apidocs.ResolveDltApiDocs;
import com.personal.marketnote.common.adapter.in.web.kafka.request.ReprocessDltRequest;
import com.personal.marketnote.common.adapter.in.web.kafka.request.ResolveDltRequest;
import com.personal.marketnote.common.adapter.in.web.kafka.response.DltMessageResponse;
import com.personal.marketnote.common.adapter.in.web.kafka.response.DltTopicSummaryResponse;
import com.personal.marketnote.common.adapter.in.web.kafka.response.ReprocessDltResponse;
import com.personal.marketnote.common.adapter.in.web.kafka.response.ResolveDltResponse;
import com.personal.marketnote.common.configuration.kafka.*;
import com.personal.marketnote.common.utility.FormatValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@Validated
@RestController
@Tag(name = "DLT API", description = "Dead Letter Topic 관리 및 재처리")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.kafka", name = "bootstrap-servers")
public class AdminDltController {

    private final DltReprocessService dltReprocessService;
    private final DltResolveService dltResolveService;
    private final DltQueryService dltQueryService;
    private final DltAuditLogger dltAuditLogger;

    @PostMapping("/api/v1/admin/kafka/dlt/reprocess")
    @PreAuthorize(ADMIN_POINTCUT)
    @ReprocessDltApiDocs
    public ResponseEntity<BaseResponse<ReprocessDltResponse>> reprocessDlt(
            @Valid @RequestBody ReprocessDltRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        String operatorInfo = resolveOperatorInfo(principal);
        DltReprocessResult result = dltReprocessService.reprocess(request.originalTopic(), operatorInfo);

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

    @PostMapping("/api/v1/admin/kafka/dlt/resolve")
    @PreAuthorize(ADMIN_POINTCUT)
    @ResolveDltApiDocs
    public ResponseEntity<BaseResponse<ResolveDltResponse>> resolveDlt(
            @Valid @RequestBody ResolveDltRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        String operatorInfo = resolveOperatorInfo(principal);
        DltResolveCommand command = new DltResolveCommand(
                request.originalTopic(), request.partition(), request.offset(),
                request.action(), request.reason()
        );
        DltResolveResult result = dltResolveService.resolve(command, operatorInfo);

        return new ResponseEntity<>(
                BaseResponse.of(
                        ResolveDltResponse.from(command, result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "DLT 메시지 해결 완료"
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/api/v1/admin/kafka/dlt")
    @PreAuthorize(ADMIN_POINTCUT)
    @QueryDltMessagesApiDocs
    public ResponseEntity<BaseResponse<List<DltMessageResponse>>> queryDltMessages(
            @RequestParam("original-topic") @NotBlank(message = "토픽명은 필수입니다") String topic,
            @RequestParam(value = "limit", defaultValue = "100") @Min(1) @Max(500) int limit,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        dltAuditLogger.logQuery(topic, limit, resolveOperatorInfo(principal));
        List<DltMessageResponse> messages = dltQueryService.queryDltMessages(topic, limit);

        return new ResponseEntity<>(
                BaseResponse.of(
                        messages,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "DLT 메시지 조회 완료"
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/api/v1/admin/kafka/dlt/summary")
    @PreAuthorize(ADMIN_POINTCUT)
    @QueryDltTopicSummariesApiDocs
    public ResponseEntity<BaseResponse<List<DltTopicSummaryResponse>>> queryDltTopicSummaries(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        dltAuditLogger.logSummaryQuery(resolveOperatorInfo(principal));
        List<DltTopicSummaryResponse> summaries = dltQueryService.queryAllDltTopicSummaries();

        return new ResponseEntity<>(
                BaseResponse.of(
                        summaries,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "DLT 토픽 요약 조회 완료"
                ),
                HttpStatus.OK
        );
    }

    private String resolveOperatorInfo(OAuth2AuthenticatedPrincipal principal) {
        if (FormatValidator.hasNoValue(principal)) {
            return "UNKNOWN";
        }
        return principal.getName();
    }
}
