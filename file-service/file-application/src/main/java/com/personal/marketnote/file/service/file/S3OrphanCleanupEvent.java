package com.personal.marketnote.file.service.file;

import java.util.List;

/**
 * @Description 도메인 트랜잭션 롤백 시 비가역으로 잔존하는 S3 객체(orphan)를 보상 정리하기 위한 도메인 이벤트.
 * Outbox 직렬화 실패 등으로 도메인 트랜잭션이 롤백되어도 S3 업로드는 비가역이므로,
 * AFTER_ROLLBACK phase에서 본 이벤트를 수신해 업로드된 객체를 삭제한다 (eventual consistency).
 */
public record S3OrphanCleanupEvent(List<String> storageUrls) {
}
