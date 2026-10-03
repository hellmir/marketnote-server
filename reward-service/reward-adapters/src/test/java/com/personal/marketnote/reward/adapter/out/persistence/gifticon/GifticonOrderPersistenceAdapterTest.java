package com.personal.marketnote.reward.adapter.out.persistence.gifticon;

import com.personal.marketnote.reward.adapter.out.persistence.gifticon.entity.GifticonOrderJpaEntity;
import com.personal.marketnote.reward.adapter.out.persistence.gifticon.repository.GifticonOrderJpaRepository;
import com.personal.marketnote.reward.domain.exception.DuplicateGifticonOrderException;
import com.personal.marketnote.reward.domain.exception.GifticonOrderNotFoundException;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrder;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderCreateState;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderSnapshotState;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderSortType;
import com.personal.marketnote.reward.domain.gifticon.GifticonOrderStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GifticonOrderPersistenceAdapter 테스트")
class GifticonOrderPersistenceAdapterTest {

    private static final String TR_ID = "NTCASH_1_20260403120000";
    private static final Long USER_ID = 1L;

    @Mock
    private GifticonOrderJpaRepository repository;

    @InjectMocks
    private GifticonOrderPersistenceAdapter adapter;

    private GifticonOrderJpaEntity buildEntity(Long id, GifticonOrderStatus status, LocalDate validEndDate) {
        GifticonOrder restored = GifticonOrder.from(GifticonOrderSnapshotState.builder()
                .id(id)
                .userId(USER_ID)
                .goodsCode("G001")
                .goodsName("테스트 상품")
                .brandName("테스트 브랜드")
                .productImageUrl("http://img.test.com/p.jpg")
                .trId(TR_ID + id)
                .orderNo("ORD" + id)
                .cashPrice(10_000L)
                .orderStatus(status)
                .couponImageUrl("http://img.test.com/c.jpg")
                .pinNo("1234-5678-9012")
                .validEndDate(validEndDate)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build());
        return GifticonOrderJpaEntity.from(restored);
    }

    @Nested
    @DisplayName("save")
    class SaveTest {

        @Test
        @DisplayName("정상 저장 시 도메인 객체를 반환한다")
        void shouldReturnDomainWhenSaveSucceeds() {
            // given
            GifticonOrder order = GifticonOrder.from(GifticonOrderCreateState.builder()
                    .userId(USER_ID)
                    .goodsCode("G001")
                    .goodsName("테스트 상품")
                    .brandName("테스트 브랜드")
                    .productImageUrl("http://img.test.com/p.jpg")
                    .trId(TR_ID)
                    .cashPrice(10_000L)
                    .build());
            given(repository.save(any(GifticonOrderJpaEntity.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            GifticonOrder saved = adapter.save(order);

            // then
            assertThat(saved.getTrId()).isEqualTo(TR_ID);
            assertThat(saved.getUserId()).isEqualTo(USER_ID);
            assertThat(saved.isPending()).isTrue();
            verify(repository).save(any(GifticonOrderJpaEntity.class));
        }

        @Test
        @DisplayName("DataIntegrityViolationException 발생 시 DuplicateGifticonOrderException을 던진다")
        void shouldThrowDuplicateExceptionWhenDataIntegrityViolation() {
            // given
            GifticonOrder order = GifticonOrder.from(GifticonOrderCreateState.builder()
                    .userId(USER_ID)
                    .goodsCode("G001")
                    .goodsName("테스트 상품")
                    .brandName("테스트 브랜드")
                    .productImageUrl("http://img.test.com/p.jpg")
                    .trId(TR_ID)
                    .cashPrice(10_000L)
                    .build());
            given(repository.save(any(GifticonOrderJpaEntity.class)))
                    .willThrow(new DataIntegrityViolationException("dup"));

            // when & then
            assertThatThrownBy(() -> adapter.save(order))
                    .isInstanceOf(DuplicateGifticonOrderException.class);
        }
    }

    @Nested
    @DisplayName("update")
    class UpdateTest {

        @Test
        @DisplayName("기존 엔티티가 존재하면 도메인 상태로 갱신한다")
        void shouldUpdateExistingEntity() {
            // given
            GifticonOrderJpaEntity existing = buildEntity(1L, GifticonOrderStatus.PENDING, null);
            given(repository.findByTrId(any())).willReturn(Optional.of(existing));

            GifticonOrder domain = existing.toDomain();
            domain.issue("http://img.test.com/c.jpg", "9999-9999-9999", "ORD-NEW", LocalDate.of(2026, 12, 31));

            // when
            adapter.update(domain);

            // then
            assertThat(existing.getOrderStatus()).isEqualTo(GifticonOrderStatus.ISSUED);
            assertThat(existing.getOrderNo()).isEqualTo("ORD-NEW");
            assertThat(existing.getPinNo()).isEqualTo("9999-9999-9999");
        }

        @Test
        @DisplayName("기존 엔티티가 없으면 GifticonOrderNotFoundException을 던진다")
        void shouldThrowNotFoundWhenNoEntity() {
            // given
            GifticonOrderJpaEntity sample = buildEntity(1L, GifticonOrderStatus.PENDING, null);
            given(repository.findByTrId(any())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adapter.update(sample.toDomain()))
                    .isInstanceOf(GifticonOrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByTrId")
    class FindByTrIdTest {

        @Test
        @DisplayName("엔티티가 존재하면 도메인을 담은 Optional을 반환한다")
        void shouldReturnDomainWhenEntityExists() {
            // given
            given(repository.findByTrId(TR_ID))
                    .willReturn(Optional.of(buildEntity(1L, GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(30))));

            // when
            Optional<GifticonOrder> result = adapter.findByTrId(TR_ID);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().isIssued()).isTrue();
        }

        @Test
        @DisplayName("엔티티가 없으면 빈 Optional을 반환한다")
        void shouldReturnEmptyWhenNoEntity() {
            given(repository.findByTrId(TR_ID)).willReturn(Optional.empty());
            assertThat(adapter.findByTrId(TR_ID)).isEmpty();
        }
    }

    @Test
    @DisplayName("existsByTrId는 리포지토리 결과를 그대로 반환한다")
    void shouldDelegateExistsByTrId() {
        given(repository.existsByTrId(TR_ID)).willReturn(true);
        assertThat(adapter.existsByTrId(TR_ID)).isTrue();
    }

    @Nested
    @DisplayName("findByUserIdAndStatuses")
    class FindByUserIdAndStatusesTest {

        @Test
        @DisplayName("PURCHASE_LATEST 정렬 시 createdAt DESC 쿼리를 호출한다")
        void shouldUsePurchaseLatestQuery() {
            // given
            given(repository.findByUserIdAndStatusesOrderByCreatedAtDesc(eq(USER_ID), anyList(), eq(-1L), any(Pageable.class)))
                    .willReturn(List.of(buildEntity(1L, GifticonOrderStatus.ISSUED, null)));

            // when
            List<GifticonOrder> orders = adapter.findByUserIdAndStatuses(
                    USER_ID, List.of(GifticonOrderStatus.ISSUED),
                    GifticonOrderSortType.PURCHASE_LATEST, -1L, 10
            );

            // then
            assertThat(orders).hasSize(1);
            verify(repository).findByUserIdAndStatusesOrderByCreatedAtDesc(eq(USER_ID), anyList(), eq(-1L), any(Pageable.class));
        }

        @Test
        @DisplayName("EXPIRY_SOONEST 정렬 시 validEndDate ASC 쿼리를 호출하고 cursor 만큼 skip한다")
        void shouldUseExpirySoonestQueryAndSkipByCursor() {
            // given
            given(repository.findByUserIdAndStatusesOrderByValidEndDateAsc(eq(USER_ID), anyList(), any(Pageable.class)))
                    .willReturn(List.of(
                            buildEntity(1L, GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(1)),
                            buildEntity(2L, GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(2)),
                            buildEntity(3L, GifticonOrderStatus.ISSUED, LocalDate.now().plusDays(3))
                    ));

            // when
            List<GifticonOrder> orders = adapter.findByUserIdAndStatuses(
                    USER_ID, List.of(GifticonOrderStatus.ISSUED),
                    GifticonOrderSortType.EXPIRY_SOONEST, 1L, 10
            );

            // then
            assertThat(orders).hasSize(2);
            assertThat(orders.get(0).getId()).isEqualTo(2L);
            assertThat(orders.get(1).getId()).isEqualTo(3L);
        }
    }

    @Test
    @DisplayName("findByIdAndUserId는 엔티티를 도메인으로 변환한다")
    void shouldFindByIdAndUserId() {
        given(repository.findByIdAndUserId(1L, USER_ID))
                .willReturn(Optional.of(buildEntity(1L, GifticonOrderStatus.ISSUED, null)));
        Optional<GifticonOrder> result = adapter.findByIdAndUserId(1L, USER_ID);
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("countByUserIdAndStatuses는 리포지토리 결과를 그대로 반환한다")
    void shouldDelegateCount() {
        given(repository.countByUserIdAndOrderStatusIn(eq(USER_ID), anyList())).willReturn(7L);
        assertThat(adapter.countByUserIdAndStatuses(USER_ID, List.of(GifticonOrderStatus.ISSUED))).isEqualTo(7L);
    }

    @Test
    @DisplayName("findAllByOrderStatus는 모든 엔티티를 도메인 리스트로 반환한다")
    void shouldFindAllByOrderStatus() {
        given(repository.findAllByOrderStatus(GifticonOrderStatus.PENDING))
                .willReturn(List.of(
                        buildEntity(1L, GifticonOrderStatus.PENDING, null),
                        buildEntity(2L, GifticonOrderStatus.PENDING, null)
                ));
        List<GifticonOrder> result = adapter.findAllByOrderStatus(GifticonOrderStatus.PENDING);
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(GifticonOrder::isPending);
    }
}
