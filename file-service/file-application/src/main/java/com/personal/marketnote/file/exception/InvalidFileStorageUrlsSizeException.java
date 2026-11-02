package com.personal.marketnote.file.exception;

public class InvalidFileStorageUrlsSizeException extends IllegalArgumentException {

    public InvalidFileStorageUrlsSizeException() {
        super("storageUrls size must match fileDomains size");
    }
}
