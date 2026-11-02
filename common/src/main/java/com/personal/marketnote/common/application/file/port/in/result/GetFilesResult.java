package com.personal.marketnote.common.application.file.port.in.result;

import java.util.List;

public record GetFilesResult(List<GetFileResult> images) {
}
