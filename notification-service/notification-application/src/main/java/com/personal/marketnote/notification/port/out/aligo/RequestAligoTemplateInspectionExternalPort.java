package com.personal.marketnote.notification.port.out.aligo;

public interface RequestAligoTemplateInspectionExternalPort {

    void requestInspection(String senderKey, String templateCode);
}
