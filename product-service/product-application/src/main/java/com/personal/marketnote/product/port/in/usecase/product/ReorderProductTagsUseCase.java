package com.personal.marketnote.product.port.in.usecase.product;

import com.personal.marketnote.product.port.in.command.ReorderProductTagsCommand;

public interface ReorderProductTagsUseCase {
    void reorderProductTags(Long userId, boolean isAdmin, ReorderProductTagsCommand command);
}
