package com.personal.marketnote.file.service.file;

import com.personal.marketnote.file.port.out.storage.UploadFilesPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class S3OrphanCleanupEventListenerTest {
    @Mock
    private UploadFilesPort uploadFilesPort;

    @InjectMocks
    private S3OrphanCleanupEventListener listener;

    @Test
    @DisplayName("storage URL 목록이 비어있으면 deleteFiles를 호출하지 않는다")
    void shouldNotInvokeDeleteWhenUrlsEmpty() {
        S3OrphanCleanupEvent event = new S3OrphanCleanupEvent(Collections.emptyList());

        listener.handleRollback(event);

        verifyNoInteractions(uploadFilesPort);
    }

    @Test
    @DisplayName("storage URL 목록이 null이면 deleteFiles를 호출하지 않는다")
    void shouldNotInvokeDeleteWhenUrlsNull() {
        S3OrphanCleanupEvent event = new S3OrphanCleanupEvent(null);

        listener.handleRollback(event);

        verifyNoInteractions(uploadFilesPort);
    }

    @Test
    @DisplayName("storage URL 목록이 존재하면 UploadFilesPort.deleteFiles를 호출한다")
    void shouldInvokeDeleteWhenUrlsPresent() {
        List<String> urls = List.of(
                "https://bucket.s3.amazonaws.com/product/1/100_a.png",
                "https://bucket.s3.amazonaws.com/product/1/200_b.png"
        );
        S3OrphanCleanupEvent event = new S3OrphanCleanupEvent(urls);

        listener.handleRollback(event);

        verify(uploadFilesPort).deleteFiles(urls);
        verify(uploadFilesPort, never()).uploadFiles(null, null, null);
    }
}
