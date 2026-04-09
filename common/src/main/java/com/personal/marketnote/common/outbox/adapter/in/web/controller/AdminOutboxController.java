package com.personal.marketnote.common.outbox.adapter.in.web.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.common.outbox.adapter.OutboxQueryService;
import com.personal.marketnote.common.outbox.adapter.OutboxResolveService;
import com.personal.marketnote.common.outbox.adapter.command.OutboxResolveCommand;
import com.personal.marketnote.common.outbox.adapter.command.OutboxResolveResult;
import com.personal.marketnote.common.outbox.adapter.in.web.controller.apidocs.QueryOutboxFailedEventsApiDocs;
import com.personal.marketnote.common.outbox.adapter.in.web.controller.apidocs.QueryOutboxFailedSummaryApiDocs;
import com.personal.marketnote.common.outbox.adapter.in.web.controller.apidocs.ResolveOutboxApiDocs;
import com.personal.marketnote.common.outbox.adapter.in.web.request.ResolveOutboxRequest;
import com.personal.marketnote.common.outbox.adapter.in.web.response.OutboxEventResponse;
import com.personal.marketnote.common.outbox.adapter.in.web.response.OutboxTopicSummaryResponse;
import com.personal.marketnote.common.outbox.adapter.in.web.response.ResolveOutboxResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@RestController
@Tag(name = "Outbox API", description = "Outbox 이벤트 관리")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "outbox", name = "enabled", havingValue = "true")
public class AdminOutboxController {

    private final OutboxQueryService outboxQueryService;
    private final OutboxResolveService outboxResolveService;

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

    @PostMapping("/api/v1/admin/outbox/resolve")
    @PreAuthorize(ADMIN_POINTCUT)
    @ResolveOutboxApiDocs
    public ResponseEntity<BaseResponse<ResolveOutboxResponse>> resolveFailedEvent(
            @Valid @RequestBody ResolveOutboxRequest request,
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        OutboxResolveCommand command = new OutboxResolveCommand(request.id(), request.action(), request.reason());
        OutboxResolveResult result = outboxResolveService.resolve(command);

        return new ResponseEntity<>(
                BaseResponse.of(
                        ResolveOutboxResponse.from(command, result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "Outbox FAILED 이벤트 해결 완료"
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
