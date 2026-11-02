package com.personal.marketnote.notification.adapter.in.web.aligo.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record SendBrandTalkRequest(
        @NotBlank String senderKey,
        @NotBlank String sender,
        @NotBlank String templateCode,
        @NotBlank String kakaoTarget,
        boolean advertYn,
        @NotNull @Size(min = 1, max = 500) @Valid List<RecipientRequest> recipients,
        FailoverRequest failover
) {

    public record RecipientRequest(
            @NotBlank String receiver,
            Map<String, String> variables
    ) {
    }

    public record FailoverRequest(
            boolean enabled,
            String subject,
            String failoverType
    ) {
    }
}
