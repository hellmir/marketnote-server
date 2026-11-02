package com.personal.marketnote.file.adapter.out.exception;

import com.personal.marketnote.common.exception.ExternalOperationFailedException;

import java.io.IOException;

public class S3UploadFailedException extends ExternalOperationFailedException {
    private static final String S3_UPLOAD_FAILED_EXCEPTION_MESSAGE = "%s::파일의 S3 저장소 업로드에 실패했습니다.";

    public S3UploadFailedException(String code, IOException cause) {
        super(String.format(S3_UPLOAD_FAILED_EXCEPTION_MESSAGE, code), cause);
    }
}
