package com.personal.marketnote.product.port.in.command;

public record RegisterCategoryCommand(
        Long parentCategoryId,
        String name
) {
    public static RegisterCategoryCommand of(Long parentCategoryId, String name) {
        return new RegisterCategoryCommand(parentCategoryId, name);
    }
}
