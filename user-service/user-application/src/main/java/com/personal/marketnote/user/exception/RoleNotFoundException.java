package com.personal.marketnote.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Getter
public class RoleNotFoundException extends NoSuchElementException {
    private final String roleId;
}
