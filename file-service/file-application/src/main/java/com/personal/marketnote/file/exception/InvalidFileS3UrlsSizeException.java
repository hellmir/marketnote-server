package com.personal.marketnote.file.exception;

public class InvalidFileS3UrlsSizeException extends IllegalArgumentException {

    public InvalidFileS3UrlsSizeException() {
        super("s3Urls size must match fileDomains size");
    }
}
