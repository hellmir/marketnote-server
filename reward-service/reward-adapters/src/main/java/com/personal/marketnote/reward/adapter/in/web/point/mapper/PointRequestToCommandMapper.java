package com.personal.marketnote.reward.adapter.in.web.point.mapper;

import com.personal.marketnote.reward.adapter.in.web.point.request.CancelPendingPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.request.ConfirmPendingPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.request.ModifyPendingPointRequest;
import com.personal.marketnote.reward.adapter.in.web.point.request.ModifyUserPointRequest;
import com.personal.marketnote.reward.port.in.command.point.CancelPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ConfirmPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyPendingPointCommand;
import com.personal.marketnote.reward.port.in.command.point.ModifyUserPointCommand;

public class PointRequestToCommandMapper {
    public static ModifyUserPointCommand mapToModifyUserPointCommand(
            Long userId,
            ModifyUserPointRequest request
    ) {
        return ModifyUserPointCommand.builder()
                .userId(userId)
                .changeType(request.getChangeType())
                .amount(request.getAmount())
                .sourceType(request.getSourceType())
                .sourceId(request.getSourceId())
                .reason(request.getReason())
                .build();
    }

    public static ModifyPendingPointCommand mapToModifyPendingPointCommand(
            Long userId,
            ModifyPendingPointRequest request
    ) {
        return ModifyPendingPointCommand.builder()
                .userId(userId)
                .changeType(request.getChangeType())
                .amount(request.getAmount())
                .sourceType(request.getSourceType())
                .sourceId(request.getSourceId())
                .reason(request.getReason())
                .build();
    }

    public static ConfirmPendingPointCommand mapToConfirmPendingPointCommand(
            Long userId,
            ConfirmPendingPointRequest request
    ) {
        return ConfirmPendingPointCommand.builder()
                .userId(userId)
                .sourceType(request.getSourceType())
                .sourceId(request.getSourceId())
                .reason(request.getReason())
                .build();
    }

    public static CancelPendingPointCommand mapToCancelPendingPointCommand(
            Long userId,
            CancelPendingPointRequest request
    ) {
        return CancelPendingPointCommand.builder()
                .userId(userId)
                .sourceType(request.getSourceType())
                .sourceId(request.getSourceId())
                .reason(request.getReason())
                .build();
    }
}

