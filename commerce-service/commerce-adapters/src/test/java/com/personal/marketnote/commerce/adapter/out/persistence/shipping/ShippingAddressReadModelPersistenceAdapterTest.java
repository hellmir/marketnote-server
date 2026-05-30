package com.personal.marketnote.commerce.adapter.out.persistence.shipping;

import com.personal.marketnote.commerce.adapter.out.persistence.shipping.entity.ShippingAddressReadModelJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.shipping.repository.ShippingAddressReadModelJpaRepository;
import com.personal.marketnote.commerce.exception.ShippingAddressNotFoundException;
import com.personal.marketnote.commerce.port.out.result.user.ShippingAddressInfoResult;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, ShippingAddressReadModelPersistenceAdapter.class})
@DisplayName("ShippingAddressReadModelPersistenceAdapter 테스트")
class ShippingAddressReadModelPersistenceAdapterTest {

    @Autowired
    private ShippingAddressReadModelPersistenceAdapter adapter;

    @Autowired
    private ShippingAddressReadModelJpaRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Nested
    @DisplayName("findByIdAndUserId")
    class FindByIdAndUserId {

        @Test
        @DisplayName("ACTIVE 상태의 배송지를 조회하여 ShippingAddressInfoResult를 반환한다")
        void returnsActiveShippingAddress() {
            // given
            adapter.upsert(1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL");

            // when
            ShippingAddressInfoResult result = adapter.findByIdAndUserId(1L, 100L);

            // then
            assertThat(result.recipientName()).isEqualTo("홍길동");
            assertThat(result.recipientPhoneNumber()).isEqualTo("010-1234-5678");
            assertThat(result.address()).isEqualTo("서울시 강남구 테헤란로 123");
            assertThat(result.addressDetail()).isEqualTo("101동 1001호");
        }

        @Test
        @DisplayName("존재하지 않는 배송지 ID로 조회 시 ShippingAddressNotFoundException이 발생한다")
        void throwsNotFoundForNonExistentId() {
            // when & then
            assertThatThrownBy(() -> adapter.findByIdAndUserId(999L, 100L))
                    .isInstanceOf(ShippingAddressNotFoundException.class);
        }

        @Test
        @DisplayName("비활성화된 배송지는 조회되지 않는다")
        void doesNotReturnInactiveAddress() {
            // given
            adapter.upsert(1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL");
            adapter.deactivateByShippingAddressId(1L);

            // when & then
            assertThatThrownBy(() -> adapter.findByIdAndUserId(1L, 100L))
                    .isInstanceOf(ShippingAddressNotFoundException.class);
        }

        @Test
        @DisplayName("userId가 일치하지 않으면 ShippingAddressNotFoundException이 발생한다")
        void throwsNotFoundForMismatchedUserId() {
            // given
            adapter.upsert(1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL");

            // when & then
            assertThatThrownBy(() -> adapter.findByIdAndUserId(1L, 999L))
                    .isInstanceOf(ShippingAddressNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("upsert")
    class Upsert {

        @Test
        @DisplayName("신규 배송지를 저장한다")
        void insertsNewAddress() {
            // when
            adapter.upsert(1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL");

            // then
            Optional<ShippingAddressReadModelJpaEntity> entity =
                    repository.findByShippingAddressIdAndUserIdAndStatus(1L, 100L, EntityStatus.ACTIVE);
            assertThat(entity).isPresent();
            assertThat(entity.get().getShippingAddressId()).isEqualTo(1L);
            assertThat(entity.get().getUserId()).isEqualTo(100L);
            assertThat(entity.get().getRecipientName()).isEqualTo("홍길동");
            assertThat(entity.get().getRecipientPhoneNumber()).isEqualTo("010-1234-5678");
            assertThat(entity.get().getAddress()).isEqualTo("서울시 강남구 테헤란로 123");
            assertThat(entity.get().getAddressDetail()).isEqualTo("101동 1001호");
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }

        @Test
        @DisplayName("동일한 shippingAddressId로 upsert 시 기존 데이터를 업데이트한다")
        void updatesExistingAddress() {
            // given
            adapter.upsert(1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL");

            // when
            adapter.upsert(1L, 100L, "김철수", "010-9876-5432", "서울시 서초구 서초대로 456", "202동 303호", "NORMAL");

            // then
            Optional<ShippingAddressReadModelJpaEntity> entity =
                    repository.findByShippingAddressIdAndUserIdAndStatus(1L, 100L, EntityStatus.ACTIVE);
            assertThat(entity).isPresent();
            assertThat(entity.get().getRecipientName()).isEqualTo("김철수");
            assertThat(entity.get().getRecipientPhoneNumber()).isEqualTo("010-9876-5432");
            assertThat(entity.get().getAddress()).isEqualTo("서울시 서초구 서초대로 456");
            assertThat(entity.get().getAddressDetail()).isEqualTo("202동 303호");
        }

        @Test
        @DisplayName("비활성화된 배송지에 대해 upsert 시 다시 활성화된다")
        void reactivatesInactiveAddress() {
            // given
            adapter.upsert(1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL");
            adapter.deactivateByShippingAddressId(1L);

            // when
            adapter.upsert(1L, 100L, "김철수", "010-9876-5432", "서울시 서초구 서초대로 456", "202동 303호", "NORMAL");

            // then
            Optional<ShippingAddressReadModelJpaEntity> entity =
                    repository.findByShippingAddressIdAndUserIdAndStatus(1L, 100L, EntityStatus.ACTIVE);
            assertThat(entity).isPresent();
            assertThat(entity.get().getRecipientName()).isEqualTo("김철수");
            assertThat(entity.get().getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("deactivateByShippingAddressId")
    class DeactivateByShippingAddressId {

        @Test
        @DisplayName("배송지를 비활성화한다")
        void deactivatesAddress() {
            // given
            adapter.upsert(1L, 100L, "홍길동", "010-1234-5678", "서울시 강남구 테헤란로 123", "101동 1001호", "NORMAL");

            // when
            adapter.deactivateByShippingAddressId(1L);

            // then
            List<ShippingAddressReadModelJpaEntity> all = repository.findAll();
            assertThat(all).hasSize(1);
            assertThat(all.getFirst().getStatus()).isEqualTo(EntityStatus.INACTIVE);
        }

        @Test
        @DisplayName("존재하지 않는 shippingAddressId 비활성화 시 에러 없이 무시한다")
        void ignoresNonExistentShippingAddressId() {
            // when & then — no exception
            adapter.deactivateByShippingAddressId(999L);
        }
    }
}
