package com.personal.marketnote.notification.adapter.in.web.notification.response;

import com.personal.marketnote.common.adapter.in.response.CursorResponse;
import com.personal.marketnote.notification.port.in.result.notification.GetNotificationHistoryResult;

import java.util.List;

public record GetNotificationHistoryResponse(
        CursorResponse<NotificationItemResponse> notifications
) {
    public static GetNotificationHistoryResponse from(GetNotificationHistoryResult result) {
        List<NotificationItemResponse> items = result.notifications().stream()
                .map(NotificationItemResponse::from)
                .toList();

        CursorResponse<NotificationItemResponse> cursorResponse = new CursorResponse<>(
                result.totalElements(),
                result.hasNext(),
                result.nextCursor(),
                items
        );

        return new GetNotificationHistoryResponse(cursorResponse);
    }
}
