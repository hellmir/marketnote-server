package com.personal.marketnote.notification.port.out.aligo;

import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessage;

import java.util.List;

public interface SendBrandTalkExternalPort {

    BrandTalkSendResponse send(AligoBrandTalkMessage message, List<byte[]> failoverImages, List<String> failoverImageFileNames);

    record BrandTalkSendResponse(
            String mid, int successCount, int failCount,
            String costType, int unitCost, int totalCost
    ) {
    }
}
