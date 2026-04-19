package com.personal.marketnote.common.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class UserNotFoundException extends DomainNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
