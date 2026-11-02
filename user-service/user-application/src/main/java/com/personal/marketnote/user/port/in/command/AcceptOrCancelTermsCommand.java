package com.personal.marketnote.user.port.in.command;

import lombok.Getter;

import java.util.List;

@Getter
public class AcceptOrCancelTermsCommand {
    private final List<Long> ids;

    private AcceptOrCancelTermsCommand(List<Long> ids) {
        this.ids = ids;
    }

    public static AcceptOrCancelTermsCommand of(List<Long> ids) {
        return new AcceptOrCancelTermsCommand(ids);
    }
}
