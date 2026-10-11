package com.personal.marketnote.notification.adapter.in.web.aligo.response;

import com.personal.marketnote.notification.port.in.result.aligo.GetAligoSenderProfileListResult;

import java.util.List;

public record GetAligoSenderProfileListResponse(
        List<AligoSenderProfileResponse> profiles
) {

    public record AligoSenderProfileResponse(
            Long id,
            String senderKey,
            String plusId,
            String name,
            String categoryCode,
            String profileInspectionStatus,
            String senderProfileStatus,
            boolean alimUseYn
    ) {
    }

    public static GetAligoSenderProfileListResponse from(GetAligoSenderProfileListResult result) {
        List<AligoSenderProfileResponse> profiles = result.profiles().stream()
                .map(p -> new AligoSenderProfileResponse(
                        p.id(), p.senderKey(), p.plusId(), p.name(),
                        p.categoryCode(), p.profileInspectionStatus(),
                        p.senderProfileStatus(), p.alimUseYn()
                ))
                .toList();
        return new GetAligoSenderProfileListResponse(profiles);
    }
}
