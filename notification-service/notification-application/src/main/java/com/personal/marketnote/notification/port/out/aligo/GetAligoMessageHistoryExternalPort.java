package com.personal.marketnote.notification.port.out.aligo;

import java.util.List;

public interface GetAligoMessageHistoryExternalPort {

    AligoMessageHistoryResult getMessageHistory(int page, int limit, String startDate, String endDate);

    record AligoMessageHistoryResult(
            int totalCount,
            List<AligoMessageHistorySummary> messages
    ) {
    }

    record AligoMessageHistorySummary(
            String mid,
            String type,
            String sender,
            String content,
            int scnt,
            int fcnt,
            String regDate
    ) {
    }
}
