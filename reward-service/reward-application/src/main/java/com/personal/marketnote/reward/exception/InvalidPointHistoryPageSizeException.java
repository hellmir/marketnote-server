package com.personal.marketnote.reward.exception;

public class InvalidPointHistoryPageSizeException extends IllegalArgumentException {
    public InvalidPointHistoryPageSizeException(int pageSize) {
        super("ERR_POINT_HISTORY_02::페이지 크기는 1 이상 100 이하여야 합니다. pageSize=" + pageSize);
    }
}
