package com.personal.marketnote.notification.adapter.out.persistence.template;

import com.personal.marketnote.common.domain.EntityStatus;
import com.personal.marketnote.notification.adapter.out.persistence.template.entity.NotificationTemplateJpaEntity;
import com.personal.marketnote.notification.adapter.out.persistence.template.repository.NotificationTemplateJpaRepository;
import com.personal.marketnote.notification.domain.template.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationTemplatePersistenceAdapterTest {

    @InjectMocks
    private NotificationTemplatePersistenceAdapter adapter;

    @Mock
    private NotificationTemplateJpaRepository notificationTemplateJpaRepository;

    @Nested
    @DisplayName("findActiveById")
    class FindActiveById {

        @Test
        @DisplayName("존재하지 않으면 empty를 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(notificationTemplateJpaRepository.findByIdAndStatus(1L, EntityStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // when
            Optional<NotificationTemplate> result = adapter.findActiveById(1L);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("존재하면 도메인 객체로 매핑하여 반환한다")
        void returnsMappedDomain() {
            // given
            NotificationTemplateJpaEntity entity = createMockEntity(1L, "ORDER_COMPLETE");
            when(notificationTemplateJpaRepository.findByIdAndStatus(1L, EntityStatus.ACTIVE))
                    .thenReturn(Optional.of(entity));

            // when
            Optional<NotificationTemplate> result = adapter.findActiveById(1L);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getTemplateCode()).isEqualTo("ORDER_COMPLETE");
        }
    }

    @Nested
    @DisplayName("findActiveByTemplateCode")
    class FindActiveByTemplateCode {

        @Test
        @DisplayName("존재하지 않으면 empty를 반환한다")
        void returnsEmptyWhenNotFound() {
            // given
            when(notificationTemplateJpaRepository.findByTemplateCodeAndStatus("UNKNOWN", EntityStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            // when
            Optional<NotificationTemplate> result = adapter.findActiveByTemplateCode("UNKNOWN");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllActive")
    class FindAllActive {

        @Test
        @DisplayName("활성 템플릿 목록을 반환한다")
        void returnsAllActiveTemplates() {
            // given
            NotificationTemplateJpaEntity entity1 = createMockEntity(1L, "ORDER_COMPLETE");
            NotificationTemplateJpaEntity entity2 = createMockEntity(2L, "SHIPPING_START");
            when(notificationTemplateJpaRepository.findAllByStatusOrderByCreatedAtDesc(EntityStatus.ACTIVE))
                    .thenReturn(List.of(entity1, entity2));

            // when
            List<NotificationTemplate> result = adapter.findAllActive();

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("템플릿을 저장하고 ID를 반환한다")
        void savesTemplateAndReturnsId() {
            // given
            NotificationTemplate template = createDomainTemplate("ORDER_COMPLETE");
            NotificationTemplateJpaEntity savedEntity = mock(NotificationTemplateJpaEntity.class);
            when(savedEntity.getId()).thenReturn(1L);
            when(notificationTemplateJpaRepository.save(any(NotificationTemplateJpaEntity.class)))
                    .thenReturn(savedEntity);

            // when
            Long savedId = adapter.save(template);

            // then
            assertThat(savedId).isEqualTo(1L);
            verify(notificationTemplateJpaRepository).save(any(NotificationTemplateJpaEntity.class));
        }

        @Test
        @DisplayName("중복 템플릿 코드이면 DuplicateNotificationTemplateException이 발생한다")
        void throwsExceptionOnDuplicate() {
            // given
            NotificationTemplate template = createDomainTemplate("ORDER_COMPLETE");
            when(notificationTemplateJpaRepository.save(any(NotificationTemplateJpaEntity.class)))
                    .thenThrow(new DataIntegrityViolationException("duplicate"));

            // when & then
            assertThatThrownBy(() -> adapter.save(template))
                    .isInstanceOf(DuplicateNotificationTemplateException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("존재하는 템플릿을 업데이트한다")
        void updatesExistingTemplate() {
            // given
            NotificationTemplate template = createDomainTemplateWithId(1L, "ORDER_COMPLETE");
            NotificationTemplateJpaEntity entity = mock(NotificationTemplateJpaEntity.class);
            when(notificationTemplateJpaRepository.findById(1L)).thenReturn(Optional.of(entity));

            // when
            adapter.update(template);

            // then
            verify(entity).updateFrom(template);
        }

        @Test
        @DisplayName("존재하지 않으면 NotificationTemplateNotFoundException이 발생한다")
        void throwsExceptionWhenNotFound() {
            // given
            NotificationTemplate template = createDomainTemplateWithId(999L, "UNKNOWN");
            when(notificationTemplateJpaRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adapter.update(template))
                    .isInstanceOf(NotificationTemplateNotFoundException.class);
        }
    }

    private NotificationTemplateJpaEntity createMockEntity(Long id, String templateCode) {
        NotificationTemplateJpaEntity entity = mock(NotificationTemplateJpaEntity.class);
        when(entity.getId()).thenReturn(id);
        when(entity.getTemplateCode()).thenReturn(templateCode);
        when(entity.getNotificationType()).thenReturn(NotificationType.ORDER_PAYMENT_COMPLETED);
        when(entity.getNotificationCategory()).thenReturn(NotificationCategory.INFORMATIONAL);
        when(entity.getTitle()).thenReturn("테스트 제목");
        when(entity.getBodyTemplate()).thenReturn("테스트 본문 {name}");
        when(entity.getUrlTemplate()).thenReturn("/orders/{orderId}");
        when(entity.getStatus()).thenReturn(EntityStatus.ACTIVE);
        return entity;
    }

    private NotificationTemplate createDomainTemplate(String templateCode) {
        return NotificationTemplate.from(
                NotificationTemplateCreateState.builder()
                        .templateCode(templateCode)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .notificationCategory(NotificationCategory.INFORMATIONAL)
                        .title("테스트 제목")
                        .bodyTemplate("테스트 본문 {name}")
                        .urlTemplate("/orders/{orderId}")
                        .build()
        );
    }

    private NotificationTemplate createDomainTemplateWithId(Long id, String templateCode) {
        return NotificationTemplate.from(
                NotificationTemplateSnapshotState.builder()
                        .id(id)
                        .templateCode(templateCode)
                        .notificationType(NotificationType.ORDER_PAYMENT_COMPLETED)
                        .notificationCategory(NotificationCategory.INFORMATIONAL)
                        .title("테스트 제목")
                        .bodyTemplate("테스트 본문 {name}")
                        .urlTemplate("/orders/{orderId}")
                        .status(EntityStatus.ACTIVE)
                        .build()
        );
    }
}
