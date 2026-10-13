package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.GetAligoMessageListResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.GetAligoMessageRecipientListResponse;
import com.personal.marketnote.notification.port.in.command.GetAligoMessageListCommand;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageListResult;
import com.personal.marketnote.notification.port.in.result.aligo.GetAligoMessageRecipientListResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.CancelAligoMessageUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoMessageListUseCase;
import com.personal.marketnote.notification.port.in.usecase.aligo.GetAligoMessageRecipientListUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@Tag(name = "(관리자) 알리고 메시지 조회", description = "알리고 알림톡 메시지 조회 API")
@SecurityRequirement(name = "bearer")
@RestController
@RequestMapping("/api/v1/admin/aligo/messages")
@RequiredArgsConstructor
public class AligoMessageController {

    private final GetAligoMessageListUseCase getAligoMessageListUseCase;
    private final GetAligoMessageRecipientListUseCase getAligoMessageRecipientListUseCase;
    private final CancelAligoMessageUseCase cancelAligoMessageUseCase;

    @GetMapping
    @PreAuthorize(ADMIN_POINTCUT)
    @Operation(summary = "메시지 목록 조회", description = "알리고 알림톡 전송 메시지 목록을 조회한다.")
    public ResponseEntity<BaseResponse<GetAligoMessageListResponse>> getMessageList(
            @RequestParam(defaultValue = "1") @jakarta.validation.constraints.Min(1) int page,
            @RequestParam(defaultValue = "20") @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(100) int limit,
            @RequestParam(name = "start-date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "end-date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        GetAligoMessageListResult result = getAligoMessageListUseCase.getMessageList(
                new GetAligoMessageListCommand(page, limit, startDate, endDate)
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetAligoMessageListResponse.from(result),
                        HttpStatus.OK, DEFAULT_SUCCESS_CODE, "메시지 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    @GetMapping("/{messageId}/recipients")
    @PreAuthorize(ADMIN_POINTCUT)
    @Operation(summary = "메시지 수신자 목록 조회", description = "특정 알림톡 메시지의 수신자별 상세 결과를 조회한다.")
    public ResponseEntity<BaseResponse<GetAligoMessageRecipientListResponse>> getRecipientList(
            @PathVariable Long messageId
    ) {
        GetAligoMessageRecipientListResult result =
                getAligoMessageRecipientListUseCase.getRecipientList(messageId);

        return new ResponseEntity<>(
                BaseResponse.of(
                        GetAligoMessageRecipientListResponse.from(result),
                        HttpStatus.OK, DEFAULT_SUCCESS_CODE, "수신자 목록 조회 성공"
                ),
                HttpStatus.OK
        );
    }

    @DeleteMapping("/{messageId}")
    @PreAuthorize(ADMIN_POINTCUT)
    @Operation(summary = "예약 발송 취소", description = "알리고 예약 발송 메시지를 취소한다. 발송 예정시각 5분 전까지만 취소 가능하다.")
    public ResponseEntity<BaseResponse<Void>> cancelMessage(@PathVariable Long messageId) {
        cancelAligoMessageUseCase.cancelMessage(messageId);

        return new ResponseEntity<>(
                BaseResponse.of(null, HttpStatus.OK, DEFAULT_SUCCESS_CODE, "예약 발송 취소 성공"),
                HttpStatus.OK
        );
    }
}
