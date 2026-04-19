package com.personal.marketnote.user.exception;

import com.personal.marketnote.common.domain.exception.DomainAlreadyExistsException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserExistsException extends DomainAlreadyExistsException {
    public UserExistsException(String message) {
        super(message);
    }
}
