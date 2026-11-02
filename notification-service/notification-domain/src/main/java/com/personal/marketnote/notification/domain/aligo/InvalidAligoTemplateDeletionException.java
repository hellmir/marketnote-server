package com.personal.marketnote.notification.domain.aligo;

public class InvalidAligoTemplateDeletionException extends IllegalStateException {

    public InvalidAligoTemplateDeletionException(String templateCode) {
        super("ERR_ALIGO_TEMPLATE_04::템플릿을 삭제할 수 없는 상태입니다. templateCode=" + templateCode);
    }
}
