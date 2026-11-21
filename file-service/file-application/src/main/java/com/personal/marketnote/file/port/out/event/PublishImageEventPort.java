package com.personal.marketnote.file.port.out.event;

import java.util.List;

/**
 * 이미지 변경 이벤트 발행 포트.
 *
 * <p>구현체({@code ImageEventKafkaProducer})는 batch 처리 중 첫 번째 직렬화 실패 시 즉시 abort 후
 * 도메인 예외를 rethrow 한다. 이로 인해 호출자가 비트랜잭션 컨텍스트에서 호출하면 {@code K+1} 번째
 * 직렬화 실패 시 이미 저장된 {@code K} 개의 Outbox 이벤트가 그대로 발행되어 부분 발행 누수가 발생한다.
 *
 * <p><b>호출 규약:</b> 모든 호출자는 반드시 {@code @Transactional} 컨텍스트에서 호출해야 한다.
 * abort 시 도메인 트랜잭션 롤백으로 부분 저장된 Outbox 이벤트도 함께 롤백되어 정합성이 유지된다.
 *
 * <p><b>현 호출자 (#5053 검증 시점):</b>
 * <ul>
 *   <li>{@code UpdateFilesService} — {@code @Transactional(isolation = READ_COMMITTED)}</li>
 *   <li>{@code DeleteFileService} — {@code @Transactional(isolation = READ_COMMITTED)}</li>
 * </ul>
 */
public interface PublishImageEventPort {

    void publishImageCreatedEvents(List<ImageEventCommand> events);

    void publishImageDeletedEvents(List<ImageEventCommand> events);
}
