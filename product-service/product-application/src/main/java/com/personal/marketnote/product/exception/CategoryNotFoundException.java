package com.personal.marketnote.product.exception;

import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import lombok.Getter;

@Getter
public class CategoryNotFoundException extends DomainNotFoundException {
    public CategoryNotFoundException(String message, Long parentCategoryId) {
        super(String.format(message, parentCategoryId));
    }
}
