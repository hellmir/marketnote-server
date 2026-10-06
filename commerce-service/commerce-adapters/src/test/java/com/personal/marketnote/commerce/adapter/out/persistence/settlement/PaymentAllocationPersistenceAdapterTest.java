package com.personal.marketnote.commerce.adapter.out.persistence.settlement;

import com.personal.marketnote.commerce.adapter.out.persistence.settlement.repository.PaymentAllocationJpaRepository;
import com.personal.marketnote.commerce.domain.ledger.IdempotencyKey;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocation;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationCreateState;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTargetType;
import com.personal.marketnote.commerce.domain.settlement.PaymentAllocationTransactionType;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import({AuditConfig.class, PaymentAllocationPersistenceAdapter.class})
@DisplayName("PaymentAllocationPersistenceAdapter 통합 테스트")
class PaymentAllocationPersistenceAdapterTest {

    @Autowired
    private PaymentAllocationPersistenceAdapter adapter;

    @Autowired
    private PaymentAllocationJpaRepository paymentAllocationJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        paymentAllocationJpaRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    private PaymentAllocation buildAllocation(Long orderId, Long sellerId, String idempotencyKey) {
        return PaymentAllocation.from(
                PaymentAllocationCreateState.builder()
                        .orderId(orderId)
                        .sellerId(sellerId)
                        .allocatedAmount(10000L)
                        .shippingFee(3000L)
                        .transactionType(PaymentAllocationTransactionType.ORDER_REGISTRATION)
                        .targetType(PaymentAllocationTargetType.ORDER)
                        .idempotencyKey(IdempotencyKey.of(idempotencyKey))
                        .build()
        );
    }

    @Nested
    @DisplayName("saveAll")
    class SaveAll {

        @Test
        @DisplayName("결제 배분 목록을 저장한다")
        void shouldSaveAll() {
            // given
            List<PaymentAllocation> allocations = List.of(
                    buildAllocation(1L, 100L, "ALLOC:1:100"),
                    buildAllocation(1L, 200L, "ALLOC:1:200")
            );

            // when
            adapter.saveAll(allocations);
            entityManager.flush();
            entityManager.clear();

            // then
            assertThat(paymentAllocationJpaRepository.findAll()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findUnsettledAllocations")
    class FindUnsettledAllocations {

        @Test
        @DisplayName("연/월 기준으로 settlementId가 NULL인 배분만 조회한다")
        void shouldReturnUnsettledAllocations() {
            // given
            adapter.saveAll(List.of(buildAllocation(1L, 100L, "ALLOC:1:100")));
            entityManager.flush();
            entityManager.clear();

            // when
            java.time.LocalDate today = java.time.LocalDate.now();
            List<PaymentAllocation> result = adapter.findUnsettledAllocations(today.getYear(), today.getMonthValue());

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSettlementId()).isNull();
        }
    }

    @Nested
    @DisplayName("findBySettlementId")
    class FindBySettlementId {

        @Test
        @DisplayName("정산 ID에 연결된 배분 목록을 조회한다")
        void shouldFindBySettlementId() {
            // given
            adapter.saveAll(List.of(
                    buildAllocation(1L, 100L, "ALLOC:1:100"),
                    buildAllocation(2L, 100L, "ALLOC:2:100")
            ));
            entityManager.flush();
            entityManager.clear();
            List<Long> ids = paymentAllocationJpaRepository.findAll().stream()
                    .map(e -> e.getId()).toList();
            adapter.assignSettlement(ids, 999L);
            entityManager.flush();
            entityManager.clear();

            // when
            List<PaymentAllocation> result = adapter.findBySettlementId(999L);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(allocation -> allocation.getSettlementId().equals(999L));
        }
    }

    @Nested
    @DisplayName("findByOrderId")
    class FindByOrderId {

        @Test
        @DisplayName("주문 ID에 연결된 배분 목록을 조회한다")
        void shouldFindByOrderId() {
            // given
            adapter.saveAll(List.of(
                    buildAllocation(1L, 100L, "ALLOC:1:100"),
                    buildAllocation(1L, 200L, "ALLOC:1:200"),
                    buildAllocation(2L, 100L, "ALLOC:2:100")
            ));
            entityManager.flush();
            entityManager.clear();

            // when
            List<PaymentAllocation> result = adapter.findByOrderId(1L);

            // then
            assertThat(result).hasSize(2);
            assertThat(result).allMatch(allocation -> allocation.getOrderId().equals(1L));
        }
    }

    @Nested
    @DisplayName("assignSettlement")
    class AssignSettlement {

        @Test
        @DisplayName("배분 ID 목록에 settlementId를 할당한다")
        void shouldAssignSettlementId() {
            // given
            adapter.saveAll(List.of(
                    buildAllocation(1L, 100L, "ALLOC:1:100"),
                    buildAllocation(2L, 100L, "ALLOC:2:100")
            ));
            entityManager.flush();
            entityManager.clear();
            List<Long> ids = paymentAllocationJpaRepository.findAll().stream()
                    .map(e -> e.getId()).toList();

            // when
            adapter.assignSettlement(ids, 555L);
            entityManager.flush();
            entityManager.clear();

            // then
            List<PaymentAllocation> result = adapter.findBySettlementId(555L);
            assertThat(result).hasSize(2);
        }
    }
}
