package com.personal.marketnote.commerce.adapter.in.web.settlement.mapper;

import com.personal.marketnote.commerce.adapter.in.web.settlement.request.ExecuteSettlementRequest;
import com.personal.marketnote.commerce.port.in.command.settlement.ExecuteSettlementCommand;
import com.personal.marketnote.commerce.port.in.command.settlement.GetSettlementsQuery;

public class SettlementRequestToCommandMapper {
    private SettlementRequestToCommandMapper() {
    }

    public static ExecuteSettlementCommand mapToCommand(ExecuteSettlementRequest request) {
        return ExecuteSettlementCommand.builder()
                .year(request.getYear())
                .month(request.getMonth())
                .pgFeeRate(request.getPgFeeRate())
                .platformFeeRate(request.getPlatformFeeRate())
                .build();
    }

    public static GetSettlementsQuery mapToQuery(Integer year, Integer month) {
        return GetSettlementsQuery.of(year, month);
    }
}
