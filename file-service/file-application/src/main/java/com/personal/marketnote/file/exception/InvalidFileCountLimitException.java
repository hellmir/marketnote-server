package com.personal.marketnote.file.exception;

public class InvalidFileCountLimitException extends IllegalArgumentException {
    private static final String MESSAGE = "파일 업로드 최대 개수를 초과했습니다. 최대: %d, 요청: %d";

    public InvalidFileCountLimitException(int maxCount, int requestedCount) {
        super(String.format(MESSAGE, maxCount, requestedCount));
    }
}
