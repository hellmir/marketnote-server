package com.personal.marketnote.user.adapter.in.web.user.mapper;

import com.personal.marketnote.user.adapter.in.web.user.request.AcceptOrCancelTermsRequest;
import com.personal.marketnote.user.port.in.command.AcceptOrCancelTermsCommand;

public class TermsRequestToCommandMapper {
    public static AcceptOrCancelTermsCommand mapToCommand(AcceptOrCancelTermsRequest acceptOrCancelTermsRequest) {
        return AcceptOrCancelTermsCommand.of(acceptOrCancelTermsRequest.getIds());
    }
}
