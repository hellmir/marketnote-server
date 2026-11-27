package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoMessageJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoMessageRecipientJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.repository.AligoMessageJpaRepository;
import com.personal.marketnote.notification.domain.aligo.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, AligoMessagePersistenceAdapter.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("AligoMessagePersistenceAdapter 테스트")
class AligoMessagePersistenceAdapterTest {

    @Autowired
    private AligoMessagePersistenceAdapter adapter;

    @Autowired
    private AligoMessageJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("save 호출 시 메시지와 수신자가 함께 저장되고 도메인이 반환된다")
    void shouldSaveMessageWithRecipients() {
        // given
        AligoMessage message = buildInitialMessage("TPL-SAVE-001", List.of("01011112222", "01033334444"));

        // when
        AligoMessage saved = adapter.save(message);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoMessageJpaEntity reloaded = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        assertThat(reloaded.getTemplateCode()).isEqualTo("TPL-SAVE-001");
        assertThat(reloaded.getMessageStatus()).isEqualTo(AligoMessageStatus.REQUESTED);
        assertThat(reloaded.getRecipients()).hasSize(2);
        assertThat(reloaded.getRecipients())
            .extracting(AligoMessageRecipientJpaEntity::getReceiver)
            .containsExactlyInAnyOrder("01011112222", "01033334444");
    }

    @Test
    @DisplayName("update 호출 시 isSent=true 면 aligoMid, successCount, failCount, sentAt 이 갱신된다")
    void shouldUpdateSendResultWhenSent() {
        // given
        AligoMessage saved = adapter.save(buildInitialMessage("TPL-SENT-001", List.of("01011112222")));
        entityManager.flush();
        entityManager.clear();

        AligoMessage reloaded = adapter.findById(saved.getId()).orElseThrow();
        LocalDateTime sentAt = LocalDateTime.of(2026, 4, 28, 10, 0);
        reloaded.markAsSent("ALIGO-MID-001", 1, 0, sentAt);

        // when
        adapter.update(reloaded);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoMessageJpaEntity entity = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        assertThat(entity.getMessageStatus()).isEqualTo(AligoMessageStatus.SENT);
        assertThat(entity.getAligoMid()).isEqualTo("ALIGO-MID-001");
        assertThat(entity.getSuccessCount()).isEqualTo(1);
        assertThat(entity.getFailCount()).isZero();
        assertThat(entity.getSentAt()).isEqualTo(sentAt);
    }

    @Test
    @DisplayName("update 호출 시 isFailovered=true 면 aligoMid, smid, 카운트, sentAt, failoverUsed 가 갱신된다")
    void shouldUpdateFailoverResultWhenFailovered() {
        // given
        AligoMessage saved = adapter.save(buildInitialMessage("TPL-FO-001", List.of("01011112222", "01033334444")));
        entityManager.flush();
        entityManager.clear();

        AligoMessage reloaded = adapter.findById(saved.getId()).orElseThrow();
        LocalDateTime sentAt = LocalDateTime.of(2026, 4, 28, 11, 0);
        reloaded.markAsFailovered("ALIGO-MID-FO", "SMID-001", 2, 0, sentAt);

        // when
        adapter.update(reloaded);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoMessageJpaEntity entity = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        assertThat(entity.getMessageStatus()).isEqualTo(AligoMessageStatus.FAILOVERED);
        assertThat(entity.getAligoMid()).isEqualTo("ALIGO-MID-FO");
        assertThat(entity.isFailoverUsed()).isTrue();
        assertThat(entity.getSuccessCount()).isEqualTo(2);
        assertThat(entity.getSentAt()).isEqualTo(sentAt);
        assertThat(entity.getRecipients())
            .extracting(AligoMessageRecipientJpaEntity::getSmid)
            .containsOnly("SMID-001");
    }

    @Test
    @DisplayName("update 호출 시 isDelivered=true 면 messageStatus 만 DELIVERED 로 갱신된다")
    void shouldUpdateOnlyStatusWhenDelivered() {
        // given
        AligoMessage saved = adapter.save(buildInitialMessage("TPL-DV-001", List.of("01011112222")));
        entityManager.flush();
        entityManager.clear();

        AligoMessage reloaded = adapter.findById(saved.getId()).orElseThrow();
        reloaded.markAsSent("ALIGO-MID-DV", 1, 0, LocalDateTime.of(2026, 4, 28, 9, 0));
        adapter.update(reloaded);
        entityManager.flush();
        entityManager.clear();

        AligoMessage sentReloaded = adapter.findById(saved.getId()).orElseThrow();
        sentReloaded.markAsDelivered();

        // when
        adapter.update(sentReloaded);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoMessageJpaEntity entity = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        assertThat(entity.getMessageStatus()).isEqualTo(AligoMessageStatus.DELIVERED);
        assertThat(entity.getAligoMid()).isEqualTo("ALIGO-MID-DV");
    }

    @Test
    @DisplayName("update 호출 시 isCancelled=true 면 messageStatus 만 CANCELLED 로 갱신된다")
    void shouldUpdateOnlyStatusWhenCancelled() {
        // given
        AligoMessage saved = adapter.save(buildInitialMessage("TPL-CANCEL-001", List.of("01011112222")));
        entityManager.flush();
        entityManager.clear();

        AligoMessage reloaded = adapter.findById(saved.getId()).orElseThrow();
        reloaded.cancel();

        // when
        adapter.update(reloaded);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoMessageJpaEntity entity = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        assertThat(entity.getMessageStatus()).isEqualTo(AligoMessageStatus.CANCELLED);
        assertThat(entity.getAligoMid()).isNull();
    }

    @Test
    @DisplayName("update 호출 시 메시지가 존재하지 않으면 AligoMessageNotFoundException 이 발생한다")
    void shouldThrowWhenUpdatingNonExistentMessage() {
        // given
        AligoMessage missing = AligoMessage.from(
            AligoMessageSnapshotState.builder()
                .id(999_999L)
                .senderKey("sender-key")
                .templateCode("TPL-MISS")
                .sender("01000000000")
                .messageType(AligoMessageType.AT)
                .content("본문")
                .messageStatus(AligoMessageStatus.FAILED)
                .successCount(0)
                .failCount(0)
                .failoverUsed(false)
                .requestedAt(LocalDateTime.of(2026, 4, 28, 9, 0))
                .recipients(List.of())
                .status(EntityStatus.ACTIVE)
                .build()
        );

        // when & then
        assertThatThrownBy(() -> adapter.update(missing))
            .isInstanceOf(AligoMessageNotFoundException.class);
    }

    @Test
    @DisplayName("update 호출 시 도메인 수신자가 resolved 면 polling 결과가 엔티티에 반영된다")
    void shouldSyncRecipientPollingResult() {
        // given
        AligoMessage saved = adapter.save(buildInitialMessage("TPL-POLL-001", List.of("01011112222", "01033334444")));
        entityManager.flush();
        entityManager.clear();

        AligoMessage reloaded = adapter.findById(saved.getId()).orElseThrow();
        reloaded.markAsSent("ALIGO-MID-POLL", 2, 0, LocalDateTime.of(2026, 4, 28, 10, 0));
        adapter.update(reloaded);
        entityManager.flush();
        entityManager.clear();

        AligoMessage sentReloaded = adapter.findById(saved.getId()).orElseThrow();
        AligoMessageRecipient firstRecipient = sentReloaded.getRecipients().get(0);
        LocalDateTime resultAt = LocalDateTime.of(2026, 4, 28, 10, 5);
        LocalDateTime reportAt = LocalDateTime.of(2026, 4, 28, 10, 6);
        sentReloaded.updateRecipientResult(
            firstRecipient.getReceiver(), AligoMessageRecipientStatus.SUCCESS,
            "MSG-001", "1", "성공",
            LocalDateTime.of(2026, 4, 28, 10, 1), resultAt, reportAt
        );

        // when
        adapter.update(sentReloaded);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoMessageJpaEntity entity = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        AligoMessageRecipientJpaEntity updated = entity.getRecipients().stream()
            .filter(r -> r.getId().equals(firstRecipient.getId()))
            .findFirst().orElseThrow();
        assertThat(updated.getRecipientStatus()).isEqualTo(AligoMessageRecipientStatus.SUCCESS);
        assertThat(updated.getMsgId()).isEqualTo("MSG-001");
        assertThat(updated.getRslt()).isEqualTo("1");
        assertThat(updated.getRsltMessage()).isEqualTo("성공");
        assertThat(updated.getResultAt()).isEqualTo(resultAt);
        assertThat(updated.getReportAt()).isEqualTo(reportAt);
    }

    @Test
    @DisplayName("findById 호출 시 수신자를 포함하여 메시지를 조회한다")
    void shouldFindByIdWithRecipients() {
        // given
        AligoMessage saved = adapter.save(buildInitialMessage("TPL-FIND-001", List.of("01011112222", "01033334444")));
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<AligoMessage> result = adapter.findById(saved.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTemplateCode()).isEqualTo("TPL-FIND-001");
        assertThat(result.get().getRecipients()).hasSize(2);
    }

    @Test
    @DisplayName("findById 호출 시 메시지가 존재하지 않으면 빈 Optional 을 반환한다")
    void shouldReturnEmptyWhenFindByIdNotFound() {
        // when
        Optional<AligoMessage> result = adapter.findById(999_999L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findPendingMessages 호출 시 SENT 상태이면서 createdAt 이 after 이후인 메시지를 반환한다")
    void shouldFindPendingMessagesByStatusAndCreatedAt() {
        // given
        AligoMessage saved = adapter.save(buildInitialMessage("TPL-PEND-001", List.of("01011112222")));
        entityManager.flush();
        entityManager.clear();

        AligoMessage reloaded = adapter.findById(saved.getId()).orElseThrow();
        reloaded.markAsSent("ALIGO-MID-PEND", 1, 0, LocalDateTime.of(2026, 4, 28, 10, 0));
        adapter.update(reloaded);
        entityManager.flush();
        entityManager.clear();

        // when
        List<AligoMessage> result = adapter.findPendingMessages(LocalDateTime.now().minusDays(1));

        // then
        assertThat(result).extracting(AligoMessage::getId).contains(saved.getId());
        assertThat(result).allSatisfy(m -> assertThat(m.getMessageStatus()).isEqualTo(AligoMessageStatus.SENT));
    }

    @Test
    @DisplayName("findPendingMessages 호출 시 after 이전에 생성된 메시지는 제외된다")
    void shouldExcludePendingMessagesBeforeAfter() {
        // given
        AligoMessage saved = adapter.save(buildInitialMessage("TPL-PEND-002", List.of("01011112222")));
        entityManager.flush();
        entityManager.clear();

        AligoMessage reloaded = adapter.findById(saved.getId()).orElseThrow();
        reloaded.markAsSent("ALIGO-MID-PEND2", 1, 0, LocalDateTime.of(2026, 4, 28, 10, 0));
        adapter.update(reloaded);
        entityManager.flush();
        entityManager.clear();

        // when
        List<AligoMessage> result = adapter.findPendingMessages(LocalDateTime.now().plusDays(1));

        // then
        assertThat(result).extracting(AligoMessage::getId).doesNotContain(saved.getId());
    }

    @Test
    @DisplayName("findMessages 호출 시 createdAt 범위 내 메시지를 페이지 단위로 반환한다")
    void shouldFindMessagesByCreatedAtRangeWithPaging() {
        // given
        AligoMessage saved1 = adapter.save(buildInitialMessage("TPL-LIST-001", List.of("01011112222")));
        AligoMessage saved2 = adapter.save(buildInitialMessage("TPL-LIST-002", List.of("01033334444")));
        entityManager.flush();
        entityManager.clear();

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // when
        List<AligoMessage> result = adapter.findMessages(start, end, 1, 10);

        // then
        assertThat(result).extracting(AligoMessage::getId)
            .contains(saved1.getId(), saved2.getId());
    }

    @Test
    @DisplayName("countMessages 호출 시 createdAt 범위 내 메시지 개수를 반환한다")
    void shouldCountMessagesByCreatedAtRange() {
        // given
        adapter.save(buildInitialMessage("TPL-COUNT-001", List.of("01011112222")));
        adapter.save(buildInitialMessage("TPL-COUNT-002", List.of("01033334444")));
        entityManager.flush();
        entityManager.clear();

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        // when
        long count = adapter.countMessages(start, end);

        // then
        assertThat(count).isGreaterThanOrEqualTo(2L);
    }

    @Test
    @DisplayName("AligoMessageJpaEntityToDomainMapper.mapToDomainSummary 는 수신자를 비운 도메인을 반환한다")
    void shouldMapToDomainSummaryWithoutRecipients() {
        // given
        AligoMessage saved = adapter.save(buildInitialMessage("TPL-SUMMARY-001", List.of("01011112222")));
        entityManager.flush();
        entityManager.clear();

        AligoMessageJpaEntity entity = repository.findById(saved.getId()).orElseThrow();

        // when
        AligoMessage domain = AligoMessageJpaEntityToDomainMapper.mapToDomainSummary(entity);

        // then
        assertThat(domain.getId()).isEqualTo(saved.getId());
        assertThat(domain.getTemplateCode()).isEqualTo("TPL-SUMMARY-001");
        assertThat(domain.getRecipients()).isEmpty();
    }

    @Test
    @DisplayName("AligoMessageJpaEntityToDomainMapper.mapToDomain 은 수신자 스냅샷을 포함한 도메인을 반환한다")
    void shouldMapToDomainWithRecipients() {
        // given
        AligoMessage saved = adapter.save(buildInitialMessage("TPL-MAP-001", List.of("01011112222", "01033334444")));
        entityManager.flush();
        entityManager.clear();

        AligoMessageJpaEntity entity = repository.findByIdWithRecipients(saved.getId()).orElseThrow();

        // when
        AligoMessage domain = AligoMessageJpaEntityToDomainMapper.mapToDomain(entity);

        // then
        assertThat(domain.getId()).isEqualTo(saved.getId());
        assertThat(domain.getRecipients()).hasSize(2);
        assertThat(domain.getRecipients())
            .extracting(AligoMessageRecipient::getReceiver)
            .containsExactlyInAnyOrder("01011112222", "01033334444");
    }

    private AligoMessage buildInitialMessage(String templateCode, List<String> receivers) {
        List<AligoMessageRecipientCreateState> recipientStates = receivers.stream()
            .map(r -> AligoMessageRecipientCreateState.builder()
                .receiver(r)
                .recvName("수신자")
                .build())
            .toList();
        return AligoMessage.from(
            AligoMessageCreateState.builder()
                .senderKey("sender-key-init")
                .templateCode(templateCode)
                .sender("01000000000")
                .messageType(AligoMessageType.AT)
                .content("본문 내용입니다.")
                .title("제목")
                .buttons("[]")
                .requestedAt(LocalDateTime.of(2026, 4, 28, 9, 0))
                .recipients(recipientStates)
                .build()
        );
    }
}
