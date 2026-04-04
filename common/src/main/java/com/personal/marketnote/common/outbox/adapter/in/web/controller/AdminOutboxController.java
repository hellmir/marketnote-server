package com.personal.marketnote.common.outbox.adapter.in.web.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.outbox.adapter.OutboxQueryService;
import com.personal.marketnote.common.outbox.adapter.in.web.controller.apidocs.QueryOutboxFailedEventsApiDocs;
import com.personal.marketnote.common.outbox.adapter.in.web.controller.apidocs.QueryOutboxFailedSummaryApiDocs;
import com.personal.marketnote.common.outbox.adapter.in.web.response.OutboxEventResponse;
import com.personal.marketnote.common.outbox.adapter.in.web.response.OutboxTopicSummaryResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@RestController
@Tag(name = "Outbox API", description = "Outbox 이벤트 관리")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "outbox", name = "enabled", havingValue = "true")
public class AdminOutboxController {

    private final OutboxQueryService outboxQueryService;

    @GetMapping("/api/v1/admin/outbox/failed")
    @PreAuthorize(ADMIN_POINTCUT)
    @QueryOutboxFailedEventsApiDocs
    public ResponseEntity<BaseResponse<List<OutboxEventResponse>>> queryFailedEvents(
            @RequestParam(value = "topic", required = false) String topic,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        List<OutboxEventResponse> events = outboxQueryService.queryFailedEvents(topic);

        return new ResponseEntity<>(
                BaseResponse.of(
                        events,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "Outbox FAILED 이벤트 조회 완료"
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/api/v1/admin/outbox/failed/summary")
    @PreAuthorize(ADMIN_POINTCUT)
    @QueryOutboxFailedSummaryApiDocs
    public ResponseEntity<BaseResponse<List<OutboxTopicSummaryResponse>>> queryFailedSummary(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        List<OutboxTopicSummaryResponse> summaries = outboxQueryService.queryFailedSummary();

        return new ResponseEntity<>(
                BaseResponse.of(
                        summaries,
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "Outbox FAILED 토픽별 요약 조회 완료"
                ),
                HttpStatus.OK
        );
    }
}
