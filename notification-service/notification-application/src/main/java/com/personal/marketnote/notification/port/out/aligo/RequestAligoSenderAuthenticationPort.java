package com.personal.marketnote.notification.port.out.aligo;

public interface RequestAligoSenderAuthenticationPort {

    String requestAuthentication(String plusId, String phoneNumber);
}
