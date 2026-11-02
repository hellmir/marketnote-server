package com.personal.marketnote.common.adapter.in.web.identification.response;

public record WhoAmIResponse(
        String publicIp
) {
    public static WhoAmIResponse of(String publicIp) {
        return new WhoAmIResponse(publicIp);
    }
}
