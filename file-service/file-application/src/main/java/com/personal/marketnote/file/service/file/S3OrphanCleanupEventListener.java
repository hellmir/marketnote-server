package com.personal.marketnote.file.service.file;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.file.port.out.storage.UploadFilesPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3OrphanCleanupEventListener {
    private final UploadFilesPort uploadFilesPort;

    /**
     * @Description 도메인 트랜잭션 롤백 시 S3 orphan 객체를 보상 정리한다.
     * AFTER_ROLLBACK phase에서 동작하므로 트랜잭션 외부에서 S3 호출이 이루어진다.
     * 비동기로 전환할 경우 self-invocation 회피와 별도 스레드 풀 격리가 필요해 동기 호출을 유지한다.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleRollback(S3OrphanCleanupEvent event) {
        if (FormatValidator.hasNoValue(event.storageUrls())) {
            return;
        }
        // storage URL은 ownerId/파일명 평문을 포함하므로 INFO/WARN 레벨에서는 건수만 노출하고,
        // 상세 URL은 운영 디버깅이 필요한 경우에만 DEBUG로 출력한다.
        log.warn(
            "도메인 트랜잭션 롤백 감지 — S3 orphan 보상 정리 시도. count={}",
            event.storageUrls().size()
        );
        if (log.isDebugEnabled()) {
            log.debug("orphan storage urls: {}", event.storageUrls());
        }
        uploadFilesPort.deleteFiles(event.storageUrls());
    }
}
