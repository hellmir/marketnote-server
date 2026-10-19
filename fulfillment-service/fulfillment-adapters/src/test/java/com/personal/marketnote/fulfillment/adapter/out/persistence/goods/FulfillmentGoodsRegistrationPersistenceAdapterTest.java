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
import java.util.UUID;

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

    private FulfillmentGoodsRegistration buildRegistration(Long id, UUID productKey) {
        return FulfillmentGoodsRegistration.from(
                FulfillmentGoodsRegistrationSnapshotState.builder()
                        .id(id)
                        .productKey(productKey)
                        .createdAt(LocalDateTime.of(2026, 4, 14, 10, 0))
                        .build()
        );
    }

    @Nested
    @DisplayName("existsByProductKey")
    class ExistsByProductKey {

        @Test
        @DisplayName("productKey에 해당하는 상품 등록이 존재하면 true를 반환한다")
        void shouldReturnTrueWhenProductExists() {
            // given
            UUID productKey = UUID.randomUUID();
            when(repository.existsByProductKey(productKey)).thenReturn(true);

            // when
            boolean result = adapter.existsByProductKey(productKey);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("productKey에 해당하는 상품 등록이 없으면 false를 반환한다")
        void shouldReturnFalseWhenProductNotExists() {
            // given
            UUID productKey = UUID.randomUUID();
            when(repository.existsByProductKey(productKey)).thenReturn(false);

            // when
            boolean result = adapter.existsByProductKey(productKey);

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
            FulfillmentGoodsRegistration registration = buildRegistration(null, UUID.randomUUID());

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
        @DisplayName("동일 productKey로 중복 저장 시 FulfillmentGoodsAlreadyRegisteredException이 발생한다")
        void shouldThrowAlreadyRegisteredExceptionWhenDuplicateProductKey() {
            // given
            FulfillmentGoodsRegistration registration = buildRegistration(null, UUID.randomUUID());
            when(repository.saveAndFlush(any(FulfillmentGoodsRegistrationJpaEntity.class)))
                    .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

            // when & then
            assertThatThrownBy(() -> adapter.save(registration))
                    .isInstanceOf(FulfillmentGoodsAlreadyRegisteredException.class);
        }
    }
}
