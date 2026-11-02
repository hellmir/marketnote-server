package com.personal.marketnote.user.port.in.result;

import com.personal.marketnote.user.domain.user.Terms;

import java.util.List;
import java.util.stream.Collectors;

public record GetTermsResult(
        List<GetTermResult> getTermResults
) {
    public static GetTermsResult from(List<Terms> terms) {
        return new GetTermsResult(terms.stream()
                .map(GetTermResult::from)
                .collect(Collectors.toList()));
    }
}
