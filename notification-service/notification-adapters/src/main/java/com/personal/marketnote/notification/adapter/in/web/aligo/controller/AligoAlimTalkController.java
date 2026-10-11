package com.personal.marketnote.notification.adapter.in.web.aligo.controller;

import com.personal.marketnote.common.adapter.in.api.format.BaseResponse;
import com.personal.marketnote.notification.adapter.in.web.aligo.request.SendAligoAlimTalkRequest;
import com.personal.marketnote.notification.adapter.in.web.aligo.response.SendAligoAlimTalkResponse;
import com.personal.marketnote.notification.port.in.command.AligoAlimTalkRecipientCommand;
import com.personal.marketnote.notification.port.in.command.SendAligoAlimTalkCommand;
import com.personal.marketnote.notification.port.in.result.aligo.SendAligoAlimTalkResult;
import com.personal.marketnote.notification.port.in.usecase.aligo.SendAligoAlimTalkUseCase;
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
import java.util.Map;

import static com.personal.marketnote.common.domain.exception.ExceptionCode.DEFAULT_SUCCESS_CODE;
import static com.personal.marketnote.common.utility.ApiConstant.ADMIN_POINTCUT;

@Tag(name = "(관리자) 알리고 알림톡 발송", description = "알리고 알림톡 발송 API")
@SecurityRequirement(name = "bearer")
@RestController
@RequestMapping("/api/v1/admin/aligo/alimtalk")
@RequiredArgsConstructor
public class AligoAlimTalkController {

    private final SendAligoAlimTalkUseCase sendAligoAlimTalkUseCase;

    @PostMapping("/send")
    @PreAuthorize(ADMIN_POINTCUT)
    public ResponseEntity<BaseResponse<SendAligoAlimTalkResponse>> sendAlimTalk(
            @Valid @RequestBody SendAligoAlimTalkRequest request
    ) {
        SendAligoAlimTalkResult result = sendAligoAlimTalkUseCase.sendAlimTalk(
                new SendAligoAlimTalkCommand(
                        request.templateCode(),
                        request.sender(),
                        FormatValidator.hasValue(request.variables()) ? request.variables() : Map.of(),
                        mapRecipients(request.recipients())
                )
        );

        return new ResponseEntity<>(
                BaseResponse.of(
                        SendAligoAlimTalkResponse.from(result),
                        HttpStatus.OK, DEFAULT_SUCCESS_CODE, "알림톡 발송 성공"
                ),
                HttpStatus.OK
        );
    }

    private List<AligoAlimTalkRecipientCommand> mapRecipients(List<SendAligoAlimTalkRequest.RecipientRequest> recipients) {
        return recipients.stream()
                .map(r -> new AligoAlimTalkRecipientCommand(r.receiver(), r.recvName()))
                .toList();
    }
}
