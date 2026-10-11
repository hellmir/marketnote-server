package com.personal.marketnote.notification.adapter.in.web.aligo.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record SendAligoAlimTalkRequest(
        @NotBlank(message = "템플릿 코드는 필수입니다")
        String templateCode,

        @NotBlank(message = "발신번호는 필수입니다")
        String sender,

        Map<String, String> variables,

        @NotNull(message = "수신자 목록은 필수입니다")
        @Size(min = 1, max = 500, message = "수신자는 1명 이상 500명 이하여야 합니다")
        @Valid
        List<RecipientRequest> recipients
) {

    public record RecipientRequest(
            @NotBlank(message = "수신자 전화번호는 필수입니다")
            String receiver,
            String recvName
    ) {
    }
}
