package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoMessageJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoMessageRecipientJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.repository.AligoMessageJpaRepository;
import com.personal.marketnote.notification.domain.aligo.AligoMessageRecipientStatus;
import com.personal.marketnote.notification.domain.aligo.AligoMessageStatus;
import com.personal.marketnote.notification.domain.aligo.AligoMessageType;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AligoMessageCascadeTest {

    @Autowired
    private AligoMessageJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("AligoMessage 저장 시 PERSIST cascade 로 recipients 가 함께 저장된다")
    void shouldCascadePersistRecipientsOnSave() {
        // given
        List<AligoMessageRecipientJpaEntity> recipients = List.of(
                AligoMessageRecipientJpaEntity.create("01011112222", "수신자A"),
                AligoMessageRecipientJpaEntity.create("01033334444", "수신자B")
        );
        AligoMessageJpaEntity message = buildMessage(recipients);

        // when
        AligoMessageJpaEntity saved = repository.save(message);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoMessageJpaEntity reloaded = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        assertThat(reloaded.getRecipients()).hasSize(2);
        assertThat(reloaded.getRecipients())
                .extracting(AligoMessageRecipientJpaEntity::getReceiver)
                .containsExactlyInAnyOrder("01011112222", "01033334444");
        assertThat(reloaded.getRecipients())
                .allSatisfy(r -> assertThat(r.getId()).isNotNull());
    }

    @Test
    @DisplayName("MERGE cascade 로 관리 상태의 recipient 필드 변경이 반영된다")
    void shouldCascadeMergeRecipientFieldChanges() {
        // given
        AligoMessageJpaEntity saved = persistMessageWithRecipient();
        Long recipientId = saved.getRecipients().get(0).getId();
        entityManager.flush();
        entityManager.clear();

        // when
        AligoMessageJpaEntity reloaded = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        reloaded.getRecipients().get(0).updateResult(
                AligoMessageRecipientStatus.SUCCESS, "msg-001", "1", "success"
        );
        entityManager.flush();
        entityManager.clear();

        // then
        AligoMessageRecipientJpaEntity result = entityManager.find(AligoMessageRecipientJpaEntity.class, recipientId);
        assertThat(result.getRecipientStatus()).isEqualTo(AligoMessageRecipientStatus.SUCCESS);
        assertThat(result.getMsgId()).isEqualTo("msg-001");
    }

    @Test
    @DisplayName("orphanRemoval 미적용: recipients.clear() 호출해도 recipient row 는 DB 에 유지된다")
    void shouldKeepRecipientRowsWhenCollectionCleared() {
        // given
        AligoMessageJpaEntity saved = persistMessageWithRecipient();
        Long recipientId = saved.getRecipients().get(0).getId();
        entityManager.flush();
        entityManager.clear();

        // when
        AligoMessageJpaEntity reloaded = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        reloaded.getRecipients().clear();
        entityManager.flush();
        entityManager.clear();

        // then
        AligoMessageRecipientJpaEntity result = entityManager.find(AligoMessageRecipientJpaEntity.class, recipientId);
        assertThat(result).as("orphanRemoval 제거로 수신자 row 가 DB 에 남아있어야 한다").isNotNull();
        assertThat(result.getReceiver()).isEqualTo("01011112222");
    }

    @Test
    @DisplayName("REMOVE cascade 미적용: AligoMessage 삭제 시 recipient 가 자동 삭제되지 않아 flush 가 실패한다")
    void shouldNotCascadeRemoveAndThrowOnDelete() {
        // given
        AligoMessageJpaEntity saved = persistMessageWithRecipient();
        Long recipientId = saved.getRecipients().get(0).getId();
        entityManager.flush();
        entityManager.clear();

        // when / then
        AligoMessageJpaEntity reloaded = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        repository.delete(reloaded);
        assertThatThrownBy(entityManager::flush)
                .as("REMOVE cascade 가 제거되어 자식을 먼저 삭제하지 않으면 flush 단계에서 예외가 발생해야 한다")
                .isInstanceOf(Exception.class);

        // 별도의 검증: DB 에서 recipient row 는 여전히 남아있다 (cascade REMOVE 면 제거됐을 row)
        entityManager.clear();
        AligoMessageRecipientJpaEntity surviving = entityManager.find(
                AligoMessageRecipientJpaEntity.class, recipientId
        );
        assertThat(surviving)
                .as("REMOVE cascade 가 없으므로 DB 에는 recipient row 가 남아있어야 한다")
                .isNotNull();
    }

    private AligoMessageJpaEntity persistMessageWithRecipient() {
        List<AligoMessageRecipientJpaEntity> recipients = new ArrayList<>();
        recipients.add(AligoMessageRecipientJpaEntity.create("01011112222", "수신자A"));
        return repository.save(buildMessage(recipients));
    }

    private AligoMessageJpaEntity buildMessage(List<AligoMessageRecipientJpaEntity> recipients) {
        return AligoMessageJpaEntity.create(
                "sender-key",
                "TEMPLATE_001",
                "01000000000",
                AligoMessageType.AT,
                "테스트 메시지 본문",
                "테스트 제목",
                null,
                AligoMessageStatus.REQUESTED,
                null,
                null,
                LocalDateTime.now(),
                null,
                recipients
        );
    }
}
