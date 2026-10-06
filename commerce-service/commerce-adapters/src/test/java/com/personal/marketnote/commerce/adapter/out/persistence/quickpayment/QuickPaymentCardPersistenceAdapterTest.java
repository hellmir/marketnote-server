package com.personal.marketnote.commerce.adapter.out.persistence.quickpayment;

import com.personal.marketnote.commerce.adapter.out.persistence.quickpayment.repository.QuickPaymentCardJpaRepository;
import com.personal.marketnote.commerce.domain.payment.MaskedCardNumber;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCard;
import com.personal.marketnote.commerce.domain.quickpayment.QuickPaymentCardCreateState;
import com.personal.marketnote.commerce.exception.QuickPaymentCardNotFoundException;
import com.personal.marketnote.common.configuration.AuditConfig;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({AuditConfig.class, QuickPaymentCardPersistenceAdapter.class})
@DisplayName("QuickPaymentCardPersistenceAdapter 통합 테스트")
class QuickPaymentCardPersistenceAdapterTest {

    @Autowired
    private QuickPaymentCardPersistenceAdapter adapter;

    @Autowired
    private QuickPaymentCardJpaRepository quickPaymentCardJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        quickPaymentCardJpaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private QuickPaymentCard createAndSaveCard(Long userId, String batchKey) {
        QuickPaymentCard card = QuickPaymentCard.from(
                QuickPaymentCardCreateState.builder()
                        .userId(userId)
                        .batchKey(batchKey)
                        .groupId("GROUP_001")
                        .cardCode("01")
                        .cardName("테스트카드")
                        .maskedCardNumber(MaskedCardNumber.of("1234-****-****-5678"))
                        .cardBinType01("1")
                        .cardBinType02("2")
                        .build()
        );
        return adapter.save(card);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("빠른결제 카드를 저장하고 ID가 할당된 도메인 객체를 반환한다")
        void shouldSaveCardAndReturnWithId() {
            // when
            QuickPaymentCard saved = createAndSaveCard(1L, "BATCH_KEY_001");

            // then
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getUserId()).isEqualTo(1L);
            assertThat(saved.getBatchKey()).isEqualTo("BATCH_KEY_001");
            assertThat(saved.getCardName()).isEqualTo("테스트카드");
            assertThat(saved.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("findActiveByIdAndUserId")
    class FindActiveByIdAndUserId {

        @Test
        @DisplayName("ACTIVE 상태 카드를 ID와 사용자 ID로 조회한다")
        void shouldFindActiveCard() {
            // given
            QuickPaymentCard saved = createAndSaveCard(1L, "BATCH_KEY_001");
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<QuickPaymentCard> found = adapter.findActiveByIdAndUserId(saved.getId(), 1L);

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getBatchKey()).isEqualTo("BATCH_KEY_001");
        }

        @Test
        @DisplayName("다른 사용자 ID로 조회 시 빈 Optional을 반환한다")
        void shouldReturnEmptyForOtherUser() {
            // given
            QuickPaymentCard saved = createAndSaveCard(1L, "BATCH_KEY_001");
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<QuickPaymentCard> found = adapter.findActiveByIdAndUserId(saved.getId(), 999L);

            // then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("INACTIVE 상태 카드는 조회되지 않는다")
        void shouldNotReturnInactiveCard() {
            // given
            QuickPaymentCard saved = createAndSaveCard(1L, "BATCH_KEY_001");
            adapter.deactivate(saved);
            entityManager.flush();
            entityManager.clear();

            // when
            Optional<QuickPaymentCard> found = adapter.findActiveByIdAndUserId(saved.getId(), 1L);

            // then
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("카드를 비활성화 상태로 변경한다")
        void shouldDeactivateCard() {
            // given
            QuickPaymentCard saved = createAndSaveCard(1L, "BATCH_KEY_001");
            entityManager.flush();
            entityManager.clear();

            // when
            adapter.deactivate(saved);
            entityManager.flush();
            entityManager.clear();

            // then
            Optional<QuickPaymentCard> found = adapter.findActiveByIdAndUserId(saved.getId(), 1L);
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 카드 비활성화 시 QuickPaymentCardNotFoundException을 던진다")
        void shouldThrowWhenCardNotFound() {
            // given
            QuickPaymentCard nonExistent = QuickPaymentCard.from(
                    QuickPaymentCardCreateState.builder()
                            .userId(1L)
                            .batchKey("BATCH_KEY_999")
                            .groupId("GROUP_001")
                            .cardCode("01")
                            .cardName("테스트카드")
                            .maskedCardNumber(MaskedCardNumber.of("1234-****-****-5678"))
                            .cardBinType01("01")
                            .cardBinType02("02")
                            .build()
            );
            QuickPaymentCard wrapped = createNonPersistedCardWithId(nonExistent, 999L);

            // when & then
            assertThatThrownBy(() -> adapter.deactivate(wrapped))
                    .isInstanceOf(QuickPaymentCardNotFoundException.class);
        }

        private QuickPaymentCard createNonPersistedCardWithId(QuickPaymentCard original, Long id) {
            try {
                java.lang.reflect.Field idField = QuickPaymentCard.class.getDeclaredField("id");
                idField.setAccessible(true);
                idField.set(original, id);
            } catch (ReflectiveOperationException roe) {
                throw new IllegalStateException(roe);
            }
            return original;
        }
    }
}
