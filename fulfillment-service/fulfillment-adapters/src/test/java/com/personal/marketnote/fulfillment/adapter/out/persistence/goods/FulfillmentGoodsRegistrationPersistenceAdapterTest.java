package com.personal.marketnote.fulfillment.adapter.out.persistence.goods;

import com.personal.marketnote.fulfillment.adapter.out.persistence.goods.entity.FulfillmentGoodsRegistrationJpaEntity;
import com.personal.marketnote.fulfillment.adapter.out.persistence.goods.repository.FulfillmentGoodsRegistrationJpaRepository;
import com.personal.marketnote.fulfillment.domain.goods.FulfillmentGoodsRegistration;
import com.personal.marketnote.fulfillment.domain.goods.FulfillmentGoodsRegistrationSnapshotState;
import com.personal.marketnote.fulfillment.exception.FulfillmentGoodsAlreadyRegisteredException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FulfillmentGoodsRegistrationPersistenceAdapter 테스트")
class FulfillmentGoodsRegistrationPersistenceAdapterTest {

    @InjectMocks
    private FulfillmentGoodsRegistrationPersistenceAdapter adapter;

    @Mock
    private FulfillmentGoodsRegistrationJpaRepository repository;

    private FulfillmentGoodsRegistration buildRegistration(Long id, Long productId) {
        return FulfillmentGoodsRegistration.from(
                FulfillmentGoodsRegistrationSnapshotState.builder()
                        .id(id)
                        .productId(productId)
                        .createdAt(LocalDateTime.of(2026, 4, 14, 10, 0))
                        .build()
        );
    }

    @Nested
    @DisplayName("existsByProductId")
    class ExistsByProductId {

        @Test
        @DisplayName("productId에 해당하는 상품 등록이 존재하면 true를 반환한다")
        void shouldReturnTrueWhenProductExists() {
            // given
            when(repository.existsByProductId(100L)).thenReturn(true);

            // when
            boolean result = adapter.existsByProductId(100L);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("productId에 해당하는 상품 등록이 없으면 false를 반환한다")
        void shouldReturnFalseWhenProductNotExists() {
            // given
            when(repository.existsByProductId(999L)).thenReturn(false);

            // when
            boolean result = adapter.existsByProductId(999L);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("save 성공")
    class SaveSuccess {

        @Test
        @DisplayName("상품 등록 도메인 객체를 JPA 엔티티로 변환하여 저장한다")
        void shouldSaveGoodsRegistrationAsJpaEntity() {
            // given
            FulfillmentGoodsRegistration registration = buildRegistration(null, 100L);

            // when
            adapter.save(registration);

            // then
            verify(repository).saveAndFlush(any(FulfillmentGoodsRegistrationJpaEntity.class));
        }
    }

    @Nested
    @DisplayName("save 실패")
    class SaveFailure {

        @Test
        @DisplayName("동일 productId로 중복 저장 시 FulfillmentGoodsAlreadyRegisteredException이 발생한다")
        void shouldThrowAlreadyRegisteredExceptionWhenDuplicateProductId() {
            // given
            FulfillmentGoodsRegistration registration = buildRegistration(null, 100L);
            when(repository.saveAndFlush(any(FulfillmentGoodsRegistrationJpaEntity.class)))
                    .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

            // when & then
            assertThatThrownBy(() -> adapter.save(registration))
                    .isInstanceOf(FulfillmentGoodsAlreadyRegisteredException.class);
        }
    }
}
