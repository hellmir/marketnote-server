package com.personal.marketnote.product.port.in.command;

public record DeleteCategoryCommand(Long categoryId) {
    public static DeleteCategoryCommand of(Long categoryId) {
        return new DeleteCategoryCommand(categoryId);
    }
}
