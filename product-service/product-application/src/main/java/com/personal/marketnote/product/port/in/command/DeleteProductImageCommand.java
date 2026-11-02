package com.personal.marketnote.product.port.in.command;

public record DeleteProductImageCommand(Long id, Long fileId) {
    public static DeleteProductImageCommand of(Long id, Long fileId) {
        return new DeleteProductImageCommand(id, fileId);
    }
}


