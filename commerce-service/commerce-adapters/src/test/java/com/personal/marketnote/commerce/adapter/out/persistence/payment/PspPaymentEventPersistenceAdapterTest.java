package com.personal.marketnote.commerce.adapter.out.persistence.payment;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.repository.PspPaymentEventJpaRepository;
import com.personal.marketnote.commerce.domain.payment.Payment;
import com.personal.marketnote.commerce.domain.payment.PaymentCreateState;
import com.personal.marketnote.commerce.domain.payment.PspPaymentEvent;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.exception.DomainNotFoundException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({AuditConfig.class, PspPaymentEventPersistenceAdapter.class})
@DisplayName("PspPaymentEventPersistenceAdapter 통합 테스트")
class PspPaymentEventPersistenceAdapterTest {

    @Autowired
    private PspPaymentEventPersistenceAdapter adapter;

    @Autowired
    private PspPaymentEventJpaRepository pspPaymentEventJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        pspPaymentEventJpaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private PspPaymentEvent createReadyEvent(Long orderId, UUID orderKey) {
        Payment payment = Payment.from(
                PaymentCreateState.builder()
                        .orderId(orderId)
                        .orderKey(orderKey)
                        .paymentAmount(50000L)
                        .build()
        );
        return PspPaymentEvent.createReady(payment, "PG_COMPANY_KEY", "PG_MARKETNOTE_KEY", "CARD");
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("PSP 결제 이벤트를 저장한다")
        void shouldSaveEvent() {
            // given
            UUID orderKey = UUID.randomUUID();
            PspPaymentEvent event = createReadyEvent(1L, orderKey);

            // when
            PspPaymentEvent saved = adapter.save(event);

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getOrderKey()).isEqualTo(orderKey.toString());
            assertThat(saved.getPgCompanyKey()).isEqualTo("PG_COMPANY_KEY");
            assertThat(saved.getPoStatus().isReady()).isTrue();
        }
    }

    @Nested
    @DisplayName("findByOrderKey")
    class FindByOrderKey {

        @Test
        @DisplayName("주문 키로 PSP 결제 이벤트를 조회한다")
        void shouldFindByOrderKey() {
            // given
            UUID orderKey = UUID.randomUUID();
            adapter.save(createReadyEvent(1L, orderKey));
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<PspPaymentEvent> found = adapter.findByOrderKey(orderKey.toString());

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getOrderKey()).isEqualTo(orderKey.toString());
        }

        @Test
        @DisplayName("주문 키가 존재하지 않으면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNotFound() {
            // when
            Optional<PspPaymentEvent> found = adapter.findByOrderKey(UUID.randomUUID().toString());

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAllByUnknownStatus")
    class FindAllByUnknownStatus {

        @Test
        @DisplayName("UNKNOWN 상태 이벤트만 반환한다")
        void shouldReturnUnknownEvents() {
            // given
            UUID orderKey = UUID.randomUUID();
            PspPaymentEvent saved = adapter.save(createReadyEvent(1L, orderKey));
            saved.startExecution();
            saved.markUnknown("ERR", "Unknown");
            adapter.update(saved);
            entityManager.flush();
            entityManager.clear();

            // when
            List<PspPaymentEvent> result = adapter.findAllByUnknownStatus();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getPoStatus().isUnknown()).isTrue();
        }

        @Test
        @DisplayName("UNKNOWN 이벤트가 없으면 빈 리스트를 반환한다")
        void shouldReturnEmptyListWhenNoUnknown() {
            // given
            adapter.save(createReadyEvent(1L, UUID.randomUUID()));
            entityManager.flush();
            entityManager.clear();

            // when
            List<PspPaymentEvent> result = adapter.findAllByUnknownStatus();

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("이벤트 상태 전이를 반영한다")
        void shouldUpdateState() {
            // given
            UUID orderKey = UUID.randomUUID();
            PspPaymentEvent saved = adapter.save(createReadyEvent(1L, orderKey));
            saved.startExecution();
            entityManager.flush();
            entityManager.clear();

            // when
            adapter.update(saved);
            entityManager.flush();
            entityManager.clear();

            // then
            PspPaymentEvent found = adapter.findByOrderKey(orderKey.toString()).orElseThrow();
            assertThat(found.getPoStatus().isExecuting()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 이벤트 update 시 DomainNotFoundException을 던진다")
        void shouldThrowWhenNotFound() {
            // given
            PspPaymentEvent nonExistent = createReadyEvent(999L, UUID.randomUUID());

            // when & then
            assertThatThrownBy(() -> adapter.update(nonExistent))
                    .isInstanceOf(DomainNotFoundException.class);
        }
    }
}
