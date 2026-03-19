package com.personal.marketnote.commerce.adapter.in.web.payment.controller;

import com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs.GetUnknownPaymentEventsApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.payment.controller.apidocs.ResolveUnknownPaymentEventApiDocs;
import com.personal.marketnote.commerce.adapter.in.web.payment.request.ResolveUnknownPaymentRequest;
import com.personal.marketnote.commerce.adapter.in.web.payment.response.GetUnknownPaymentEventsResponse;
import com.personal.marketnote.commerce.adapter.in.web.payment.response.ResolveUnknownPaymentResponse;
import com.personal.marketnote.commerce.port.in.command.payment.ResolveUnknownPaymentCommand;
import com.personal.marketnote.commerce.port.in.result.payment.GetUnknownPaymentEventsResult;
import com.personal.marketnote.commerce.port.in.result.payment.ResolveUnknownPaymentResult;
import com.personal.marketnote.commerce.port.in.usecase.payment.GetUnknownPaymentEventsUseCase;
import com.personal.marketnote.commerce.port.in.usecase.payment.ResolveUnknownPaymentUseCase;
import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@RestController
@Tag(name = "결제 이벤트 API", description = "UNKNOWN 상태 결제 이벤트 관리")
@RequiredArgsConstructor
public class AdminPaymentEventController {
    private final GetUnknownPaymentEventsUseCase getUnknownPaymentEventsUseCase;
    private final ResolveUnknownPaymentUseCase resolveUnknownPaymentUseCase;

    @GetMapping("/api/v1/admin/payments/events/unknown")
    @PreAuthorize(ADMIN_POINTCUT)
    @GetUnknownPaymentEventsApiDocs
    public ResponseEntity<BaseResponse<List<GetUnknownPaymentEventsResponse>>> getUnknownPaymentEvents() {
        List<GetUnknownPaymentEventsResult> results = getUnknownPaymentEventsUseCase.getUnknownPaymentEvents();
        List<GetUnknownPaymentEventsResponse> responses = results.stream()
                .map(GetUnknownPaymentEventsResponse::from)
                .toList();
        return new ResponseEntity<>(
                BaseResponse.of(responses, HttpStatus.OK, DEFAULT_SUCCESS_CODE, "UNKNOWN 결제 이벤트 조회 성공"),
                HttpStatus.OK
        );
    }

    @PostMapping("/api/v1/admin/payments/events/{orderKey}/resolve")
    @PreAuthorize(ADMIN_POINTCUT)
    @ResolveUnknownPaymentEventApiDocs
    public ResponseEntity<BaseResponse<ResolveUnknownPaymentResponse>> resolveUnknownPaymentEvent(
            @PathVariable("orderKey") String orderKey,
            @Valid @RequestBody ResolveUnknownPaymentRequest request
    ) {
        ResolveUnknownPaymentCommand command = ResolveUnknownPaymentCommand.builder()
                .orderKey(orderKey)
                .resolvedStatus(request.resolvedStatus())
                .resultCode(request.resultCode())
                .resultMessage(request.resultMessage())
                .pgPaymentKey(request.pgPaymentKey())
                .approvalNumber(request.approvalNumber())
                .appTime(request.appTime())
                .build();

        ResolveUnknownPaymentResult result = resolveUnknownPaymentUseCase.resolve(command);

        return new ResponseEntity<>(
                BaseResponse.of(
                        ResolveUnknownPaymentResponse.from(result),
                        HttpStatus.OK,
                        DEFAULT_SUCCESS_CODE,
                        "UNKNOWN 결제 이벤트 해소 성공"
                ),
                HttpStatus.OK
        );
    }
}
