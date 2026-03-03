package com.personal.marketnote.commerce.adapter.in.web.settlement.mapper;

import com.personal.marketnote.commerce.adapter.in.web.settlement.request.RegisterSettlementPolicyRequest;
import com.personal.marketnote.commerce.adapter.in.web.settlement.request.UpdateSettlementPolicyRequest;
import com.personal.marketnote.commerce.port.in.command.settlement.RegisterSettlementPolicyCommand;
import com.personal.marketnote.commerce.port.in.command.settlement.UpdateSettlementPolicyCommand;

public class SettlementPolicyRequestToCommandMapper {
    private SettlementPolicyRequestToCommandMapper() {
    }

    public static RegisterSettlementPolicyCommand mapToRegisterCommand(RegisterSettlementPolicyRequest request) {
        return RegisterSettlementPolicyCommand.builder()
                .sellerId(request.getSellerId())
                .pgFeeRate(request.getPgFeeRate())
                .platformFeeRate(request.getPlatformFeeRate())
                .settlementCycle(request.getSettlementCycle())
                .minPayoutAmount(request.getMinPayoutAmount())
                .build();
    }

    public static UpdateSettlementPolicyCommand mapToUpdateCommand(Long id, UpdateSettlementPolicyRequest request) {
        return UpdateSettlementPolicyCommand.builder()
                .id(id)
                .pgFeeRate(request.getPgFeeRate())
                .platformFeeRate(request.getPlatformFeeRate())
                .settlementCycle(request.getSettlementCycle())
                .minPayoutAmount(request.getMinPayoutAmount())
                .build();
    }
}
