package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.entity.AligoBrandTalkMessageJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.repository.AligoBrandTalkMessageJpaRepository;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessage;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageNotFoundException;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageRecipientCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkMessageSnapshotState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkKakaoTarget;
import com.personal.marketnote.notification.domain.aligo.BrandTalkMessageCostType;
import com.personal.marketnote.notification.domain.aligo.BrandTalkMessageStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, AligoBrandTalkMessagePersistenceAdapter.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("AligoBrandTalkMessagePersistenceAdapter 테스트")
class AligoBrandTalkMessagePersistenceAdapterTest {

    @Autowired
    private AligoBrandTalkMessagePersistenceAdapter adapter;

    @Autowired
    private AligoBrandTalkMessageJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("save 호출 시 수신자 엔티티들이 일괄 생성되어 저장된다")
    void shouldSaveRecipientsAsBatch() {
        // given
        AligoBrandTalkMessage message = AligoBrandTalkMessage.from(
                AligoBrandTalkMessageCreateState.builder()
                        .senderKey("sender-key-save")
                        .sender("01000000000")
                        .kakaoTarget(BrandTalkKakaoTarget.M)
                        .advertYn(false)
                        .templateCode("TPL-MSG-001")
                        .failoverEnabled(false)
                        .requestedAt(LocalDateTime.now())
                        .recipients(List.of(
                                AligoBrandTalkMessageRecipientCreateState.builder()
                                        .receiver("01011112222")
                                        .receiverMessage("{\"body\":\"msg1\"}")
                                        .build(),
                                AligoBrandTalkMessageRecipientCreateState.builder()
                                        .receiver("01033334444")
                                        .receiverMessage("{\"body\":\"msg2\"}")
                                        .build(),
                                AligoBrandTalkMessageRecipientCreateState.builder()
                                        .receiver("01055556666")
                                        .receiverMessage("{\"body\":\"msg3\"}")
                                        .build()
                        ))
                        .build()
        );

        // when
        AligoBrandTalkMessage saved = adapter.save(message);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoBrandTalkMessageJpaEntity reloaded = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        assertThat(reloaded.getRecipients()).hasSize(3);
        assertThat(reloaded.getRecipients())
                .extracting("receiver")
                .containsExactlyInAnyOrder("01011112222", "01033334444", "01055556666");
        assertThat(reloaded.getRecipients())
                .allSatisfy(r -> assertThat(r.getId()).isNotNull());
    }

    @Test
    @DisplayName("update 호출 시 isSent=true 이면 aligoMid, costType, cost, status, sentAt 이 모두 갱신된다")
    void shouldUpdateAllFieldsWhenSent() {
        // given
        AligoBrandTalkMessage saved = adapter.save(buildInitialMessage("TPL-SENT-001"));
        entityManager.flush();
        entityManager.clear();

        LocalDateTime sentAt = LocalDateTime.of(2026, 4, 20, 10, 30);
        AligoBrandTalkMessage sent = AligoBrandTalkMessage.from(
                AligoBrandTalkMessageSnapshotState.builder()
                        .id(saved.getId())
                        .senderKey(saved.getSenderKey())
                        .sender(saved.getSender())
                        .kakaoTarget(saved.getKakaoTarget())
                        .advertYn(saved.isAdvertYn())
                        .templateCode(saved.getTemplateCode())
                        .messageStatus(BrandTalkMessageStatus.SENT)
                        .costType(BrandTalkMessageCostType.BNF)
                        .unitCost(15)
                        .totalCost(30)
                        .aligoMid("ALIGO-MID-001")
                        .successCount(2)
                        .failCount(0)
                        .failoverEnabled(false)
                        .requestedAt(saved.getRequestedAt())
                        .sentAt(sentAt)
                        .recipients(List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );

        // when
        adapter.update(sent);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoBrandTalkMessageJpaEntity entity = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        assertThat(entity.getAligoMid()).isEqualTo("ALIGO-MID-001");
        assertThat(entity.getCostType()).isEqualTo(BrandTalkMessageCostType.BNF);
        assertThat(entity.getUnitCost()).isEqualTo(15);
        assertThat(entity.getTotalCost()).isEqualTo(30);
        assertThat(entity.getMessageStatus()).isEqualTo(BrandTalkMessageStatus.SENT);
        assertThat(entity.getSuccessCount()).isEqualTo(2);
        assertThat(entity.getFailCount()).isZero();
        assertThat(entity.getSentAt()).isEqualTo(sentAt);
    }

    @Test
    @DisplayName("update 호출 시 isSent=false(FAILED) 이면 messageStatus 만 갱신되고 다른 필드는 유지된다")
    void shouldUpdateOnlyStatusWhenNotSent() {
        // given
        AligoBrandTalkMessage saved = adapter.save(buildInitialMessage("TPL-FAILED-001"));
        entityManager.flush();
        entityManager.clear();

        AligoBrandTalkMessage failed = AligoBrandTalkMessage.from(
                AligoBrandTalkMessageSnapshotState.builder()
                        .id(saved.getId())
                        .senderKey(saved.getSenderKey())
                        .sender(saved.getSender())
                        .kakaoTarget(saved.getKakaoTarget())
                        .advertYn(saved.isAdvertYn())
                        .templateCode(saved.getTemplateCode())
                        .messageStatus(BrandTalkMessageStatus.FAILED)
                        .aligoMid("should-not-apply")
                        .unitCost(99)
                        .totalCost(999)
                        .successCount(0)
                        .failCount(2)
                        .failoverEnabled(false)
                        .requestedAt(saved.getRequestedAt())
                        .recipients(List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );

        // when
        adapter.update(failed);
        entityManager.flush();
        entityManager.clear();

        // then
        AligoBrandTalkMessageJpaEntity entity = repository.findByIdWithRecipients(saved.getId()).orElseThrow();
        assertThat(entity.getMessageStatus()).isEqualTo(BrandTalkMessageStatus.FAILED);
        assertThat(entity.getAligoMid()).isNull();
        assertThat(entity.getCostType()).isNull();
        assertThat(entity.getUnitCost()).isNull();
        assertThat(entity.getTotalCost()).isNull();
        assertThat(entity.getSuccessCount()).isZero();
        assertThat(entity.getFailCount()).isZero();
        assertThat(entity.getSentAt()).isNull();
    }

    @Test
    @DisplayName("update 호출 시 메시지가 존재하지 않으면 AligoBrandTalkMessageNotFoundException 이 발생한다")
    void shouldThrowWhenUpdatingNonExistentMessage() {
        // given
        AligoBrandTalkMessage missing = AligoBrandTalkMessage.from(
                AligoBrandTalkMessageSnapshotState.builder()
                        .id(999L)
                        .senderKey("sender-key")
                        .sender("01000000000")
                        .kakaoTarget(BrandTalkKakaoTarget.M)
                        .advertYn(false)
                        .templateCode("TPL")
                        .messageStatus(BrandTalkMessageStatus.FAILED)
                        .successCount(0)
                        .failCount(0)
                        .failoverEnabled(false)
                        .requestedAt(LocalDateTime.now())
                        .recipients(List.of())
                        .status(EntityStatus.ACTIVE)
                        .build()
        );

        // when & then
        assertThatThrownBy(() -> adapter.update(missing))
                .isInstanceOf(AligoBrandTalkMessageNotFoundException.class);
    }

    @Test
    @DisplayName("findById 호출 시 수신자 포함하여 메시지를 조회한다")
    void shouldFindByIdWithRecipients() {
        // given
        AligoBrandTalkMessage saved = adapter.save(buildInitialMessage("TPL-FIND-001"));
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<AligoBrandTalkMessage> result = adapter.findById(saved.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTemplateCode()).isEqualTo("TPL-FIND-001");
        assertThat(result.get().getRecipients()).hasSize(1);
    }

    @Test
    @DisplayName("findById 메서드는 @Transactional(readOnly = true, READ_COMMITTED) 를 선언한다")
    void shouldDeclareReadOnlyTransactionOnFindById() throws NoSuchMethodException {
        // given
        Method method = AligoBrandTalkMessagePersistenceAdapter.class.getMethod("findById", Long.class);
        Transactional txAnnotation = method.getAnnotation(Transactional.class);

        // then
        assertThat(txAnnotation).isNotNull();
        assertThat(txAnnotation.readOnly()).isTrue();
        assertThat(txAnnotation.isolation()).isEqualTo(Isolation.READ_COMMITTED);
    }

    private AligoBrandTalkMessage buildInitialMessage(String templateCode) {
        return AligoBrandTalkMessage.from(
                AligoBrandTalkMessageCreateState.builder()
                        .senderKey("sender-key-init")
                        .sender("01000000000")
                        .kakaoTarget(BrandTalkKakaoTarget.M)
                        .advertYn(false)
                        .templateCode(templateCode)
                        .failoverEnabled(false)
                        .requestedAt(LocalDateTime.of(2026, 4, 20, 9, 0))
                        .recipients(List.of(
                                AligoBrandTalkMessageRecipientCreateState.builder()
                                        .receiver("01011112222")
                                        .receiverMessage("{\"body\":\"msg\"}")
                                        .build()
                        ))
                        .build()
        );
    }

}
