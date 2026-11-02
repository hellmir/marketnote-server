package com.personal.marketnote.product.port.in.command;

public record DeleteProductCommand(Long id) {
    public static DeleteProductCommand of(Long id) {
        return new DeleteProductCommand(id);
    }
}


