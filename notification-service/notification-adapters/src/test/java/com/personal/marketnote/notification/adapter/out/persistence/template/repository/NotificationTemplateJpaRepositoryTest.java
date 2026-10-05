package com.personal.marketnote.notification.adapter.out.persistence.template.repository;

import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.template.entity.NotificationTemplateJpaEntity;
import com.personal.marketnote.notification.domain.template.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NotificationTemplateJpaRepositoryTest {

    @Autowired
    private NotificationTemplateJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("ID와 상태로 템플릿을 조회한다")
    void shouldFindByIdAndStatus() {
        // given
        NotificationTemplateJpaEntity entity = persistTemplate("FIND_BY_ID_TEST");

        // when
        Optional<NotificationTemplateJpaEntity> result = repository.findByIdAndStatus(entity.getId(), EntityStatus.ACTIVE);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getTemplateCode()).isEqualTo("FIND_BY_ID_TEST");
    }

    @Test
    @DisplayName("비활성 템플릿은 조회되지 않는다")
    void shouldNotFindInactiveById() {
        // given
        NotificationTemplateJpaEntity entity = persistTemplate("INACTIVE_TEST");
        entityManager.createNativeQuery("UPDATE notification_template SET status = 'INACTIVE' WHERE id = :id")
                .setParameter("id", entity.getId())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // when
        Optional<NotificationTemplateJpaEntity> result = repository.findByIdAndStatus(entity.getId(), EntityStatus.ACTIVE);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("템플릿 코드와 상태로 조회한다")
    void shouldFindByTemplateCodeAndStatus() {
        // given
        persistTemplate("ORDER_COMPLETE_CODE");

        // when
        Optional<NotificationTemplateJpaEntity> result = repository.findByTemplateCodeAndStatus(
                "ORDER_COMPLETE_CODE", EntityStatus.ACTIVE
        );

        // then
        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("활성 템플릿 목록을 생성일 내림차순으로 반환한다")
    void shouldFindAllActiveOrderByCreatedAtDesc() {
        // given
        persistTemplate("TEMPLATE_A");
        persistTemplate("TEMPLATE_B");

        // when
        List<NotificationTemplateJpaEntity> result = repository.findAllByStatusOrderByCreatedAtDesc(EntityStatus.ACTIVE);

        // then
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);
    }

    private NotificationTemplateJpaEntity persistTemplate(String templateCode) {
        NotificationTemplate domain = NotificationTemplate.from(
                NotificationTemplateCreateState.builder()
                        .templateCode(templateCode)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .notificationCategory(NotificationCategory.INFORMATIONAL)
                        .title("테스트 제목")
                        .bodyTemplate("테스트 본문 {name}")
                        .urlTemplate("/test/{id}")
                        .build()
        );
        NotificationTemplateJpaEntity entity = NotificationTemplateJpaEntity.from(domain);
        NotificationTemplateJpaEntity saved = repository.saveAndFlush(entity);
        entityManager.clear();
        return saved;
    }
}
