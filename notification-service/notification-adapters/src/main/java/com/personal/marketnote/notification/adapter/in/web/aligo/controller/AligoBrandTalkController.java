package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.SendBrandTalkRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.SendBrandTalkResponse;
import com.personal.marketnote.notification.port.in.command.BrandTalkFailoverCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkRecipientCommand;
import com.personal.marketnote.notification.port.in.command.SendBrandTalkCommand;
import com.personal.marketnote.notification.port.in.result.aligo.SendBrandTalkResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.SendBrandTalkUseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@Tag(name = "(관리자) 알리고 브랜드메시지 전송", description = "알리고 브랜드메시지 전송 API")
@SecurityRequirement(name = "bearer")
@RestController
@RequestMapping("/api/v1/admin/aligo/brandtalk")
@RequiredArgsConstructor
public class AligoBrandTalkController {

    private final SendBrandTalkUseCase sendBrandTalkUseCase;

    @PostMapping("/send")
    @PreAuthorize(ADMIN_POINTCUT)
    public ResponseEntity<BaseResponse<SendBrandTalkResponse>> sendBrandTalk(
            @Valid @RequestBody SendBrandTalkRequest request
    ) {
        List<BrandTalkRecipientCommand> recipientCommands = request.recipients().stream()
                .map(r -> new BrandTalkRecipientCommand(r.receiver(), r.variables()))
                .toList();

        BrandTalkFailoverCommand failoverCommand = mapFailover(request.failover());

        SendBrandTalkCommand command = new SendBrandTalkCommand(
                request.senderKey(), request.sender(), request.templateCode(),
                request.kakaoTarget(), request.advertYn(), recipientCommands, failoverCommand
        );

        SendBrandTalkResult result = sendBrandTalkUseCase.sendBrandTalk(command);
        SendBrandTalkResponse response = SendBrandTalkResponse.from(result);

        return new ResponseEntity<>(
                BaseResponse.of(response, HttpStatus.OK, DEFAULT_SUCCESS_CODE, "브랜드메시지 발송 성공"),
                HttpStatus.OK
        );
    }

    private BrandTalkFailoverCommand mapFailover(SendBrandTalkRequest.FailoverRequest failover) {
        if (FormatValidator.hasNoValue(failover)) {
            return null;
        }
        return new BrandTalkFailoverCommand(
                failover.enabled(), failover.subject(), failover.failoverType(),
                List.of(), List.of()
        );
    }
}
