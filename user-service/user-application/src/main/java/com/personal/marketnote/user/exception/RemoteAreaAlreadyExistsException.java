package com.personal.marketnote.user.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;

public class RemoteAreaAlreadyExistsException extends DomainAlreadyExistsException {

    public RemoteAreaAlreadyExistsException(String province, String district, String village, String subarea) {
        super("ERR_REMOTE_AREA_06::이미 등록된 도서산간 지역입니다. province=" + province + ", district=" + district + ", village=" + village + ", subarea=" + subarea);
    }
}
