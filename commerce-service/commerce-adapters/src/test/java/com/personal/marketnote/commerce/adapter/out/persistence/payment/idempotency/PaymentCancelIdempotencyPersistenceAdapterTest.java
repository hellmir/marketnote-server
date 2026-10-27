package com.personal.marketnote.commerce.adapter.out.persistence.payment.idempotency;

import com.personal.marketnote.commerce.adapter.out.persistence.payment.idempotency.repository.PaymentCancelIdempotencyJpaRepository;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotency;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotencyCreateState;
import com.personal.marketnote.commerce.domain.payment.idempotency.PaymentCancelIdempotencyStatus;
import com.personal.marketnote.commerce.exception.DuplicateIdempotencyKeyException;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.QuerydslConfig;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({AuditConfig.class, QuerydslConfig.class, PaymentCancelIdempotencyPersistenceAdapter.class})
@DisplayName("PaymentCancelIdempotencyPersistenceAdapter 통합 테스트")
class PaymentCancelIdempotencyPersistenceAdapterTest {

    @Autowired
    private PaymentCancelIdempotencyPersistenceAdapter adapter;

    @Autowired
    private PaymentCancelIdempotencyJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    private static final String KEY = "11111111-2222-4333-8444-555555555555";
    private static final String KEY2 = "99999999-aaaa-4bbb-8ccc-dddddddddddd";

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("save")
    class SaveTest {

        @Test
        @DisplayName("새 레코드를 저장하면 IN_PROGRESS 로 영속화된다")
        void shouldSaveAsInProgress() {
            PaymentCancelIdempotency saved = adapter.save(newIdempotency(KEY));

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getStatus()).isEqualTo(PaymentCancelIdempotencyStatus.IN_PROGRESS);
            assertThat(saved.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("동일 idempotencyKey 중복 저장 시 DuplicateIdempotencyKeyException 이 발생한다")
        void shouldThrowWhenDuplicateKey() {
            adapter.save(newIdempotency(KEY));
            entityManager.flush();
            entityManager.clear();

            assertThatThrownBy(() -> adapter.save(newIdempotency(KEY)))
                    .isInstanceOf(DuplicateIdempotencyKeyException.class);
        }
    }

    @Nested
    @DisplayName("findByIdempotencyKey")
    class FindTest {

        @Test
        @DisplayName("존재하는 키로 조회하면 도메인을 반환한다")
        void shouldReturnDomainWhenExists() {
            adapter.save(newIdempotency(KEY));
            entityManager.flush();
            entityManager.clear();

            Optional<PaymentCancelIdempotency> found = adapter.findByIdempotencyKey(KEY);

            assertThat(found).isPresent();
            assertThat(found.get().getIdempotencyKey()).isEqualTo(KEY);
        }

        @Test
        @DisplayName("없는 키로 조회하면 Optional.empty 를 반환한다")
        void shouldReturnEmptyWhenAbsent() {
            Optional<PaymentCancelIdempotency> found = adapter.findByIdempotencyKey("missing-key");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("IN_PROGRESS → COMPLETED 전이 후 update 호출 시 DB 에 반영된다")
        void shouldUpdateStatusAndResult() {
            PaymentCancelIdempotency saved = adapter.save(newIdempotency(KEY));
            entityManager.flush();
            entityManager.clear();

            saved.complete("{\"ok\":true}");
            adapter.update(saved);
            entityManager.flush();
            entityManager.clear();

            PaymentCancelIdempotency reloaded = adapter.findByIdempotencyKey(KEY).orElseThrow();
            assertThat(reloaded.getStatus()).isEqualTo(PaymentCancelIdempotencyStatus.COMPLETED);
            assertThat(reloaded.getResult()).contains("ok");
        }
    }

    @Nested
    @DisplayName("deleteOlderThan (TTL 배치)")
    class DeleteTest {

        @Test
        @DisplayName("threshold 이전 생성 레코드만 삭제한다")
        void shouldDeleteOnlyOlderRecords() {
            adapter.save(newIdempotency(KEY));
            adapter.save(newIdempotency(KEY2));
            entityManager.flush();
            entityManager.clear();

            // 아직 갓 생성된 시점이므로 (now - 1일) 이전 레코드는 없다
            int deletedNone = adapter.deleteOlderThan(LocalDateTime.now().minusDays(1));
            assertThat(deletedNone).isZero();

            // (now + 1초) 이전은 모두 포함
            int deletedAll = adapter.deleteOlderThan(LocalDateTime.now().plusSeconds(1));
            assertThat(deletedAll).isEqualTo(2);
            assertThat(repository.count()).isZero();
        }
    }

    private PaymentCancelIdempotency newIdempotency(String key) {
        return PaymentCancelIdempotency.from(
                PaymentCancelIdempotencyCreateState.builder()
                        .idempotencyKey(key)
                        .paymentId(1L)
                        .buyerId(100L)
                        .build()
        );
    }
}
