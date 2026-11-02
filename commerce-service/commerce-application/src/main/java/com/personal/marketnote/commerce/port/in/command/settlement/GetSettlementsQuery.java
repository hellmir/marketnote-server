package com.personal.marketnote.commerce.port.in.command.settlement;

public record GetSettlementsQuery(
        Integer year,
        Integer month
) {
    public static GetSettlementsQuery of(Integer year, Integer month) {
        return new GetSettlementsQuery(year, month);
    }
}
