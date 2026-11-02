package com.personal.marketnote.notification.port.in.usecase.aligo;

import com.personal.marketnote.notification.port.in.result.aligo.RequestAligoTemplateInspectionResult;

public interface RequestAligoTemplateInspectionUseCase {

    RequestAligoTemplateInspectionResult requestInspection(String templateCode);
}
