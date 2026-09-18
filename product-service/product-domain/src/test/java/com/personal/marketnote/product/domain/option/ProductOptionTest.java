package com.personal.marketnote.product.domain.option;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProductOption 테스트")
class ProductOptionTest {

    @Nested
    @DisplayName("from(CreateState)")
    class FromCreateState {

        @Test
        @DisplayName("CreateState로 생성하면 ACTIVE 상태로 생성된다")
        void shouldCreateWithActiveStatus() {
            // given
            ProductOptionCreateState state = ProductOptionCreateState.builder()
                    .category(null)
                    .content("빨강")
                    .build();

            // when
            ProductOption option = ProductOption.from(state);

            // then
            assertThat(option.getContent()).isEqualTo("빨강");
            assertThat(option.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("from(SnapshotState)")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState의 모든 필드가 그대로 복원된다")
        void shouldRestoreAllFieldsFromSnapshotState() {
            // given
            ProductOptionSnapshotState state = ProductOptionSnapshotState.builder()
                    .id(10L)
                    .category(null)
                    .content("파랑")
                    .status(EntityStatus.INACTIVE)
                    .build();

            // when
            ProductOption option = ProductOption.from(state);

            // then
            assertThat(option.getId()).isEqualTo(10L);
            assertThat(option.getContent()).isEqualTo("파랑");
            assertThat(option.getStatus()).isEqualTo(EntityStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("addContent()")
    class AddContent {

        @Test
        @DisplayName("ids에 자신의 id가 포함되면 contents에 content를 추가한다")
        void shouldAddContentWhenIdIsContained() {
            // given
            ProductOption option = ProductOption.from(ProductOptionSnapshotState.builder()
                    .id(5L)
                    .content("빨강")
                    .status(EntityStatus.ACTIVE)
                    .build());
            Set<Long> ids = Set.of(5L, 10L);
            Set<String> contents = new HashSet<>();

            // when
            option.addContent(ids, contents);

            // then
            assertThat(contents).containsExactly("빨강");
        }

        @Test
        @DisplayName("ids에 자신의 id가 포함되지 않으면 contents에 추가하지 않는다")
        void shouldNotAddContentWhenIdIsNotContained() {
            // given
            ProductOption option = ProductOption.from(ProductOptionSnapshotState.builder()
                    .id(5L)
                    .content("빨강")
                    .status(EntityStatus.ACTIVE)
                    .build());
            Set<Long> ids = Set.of(10L, 20L);
            Set<String> contents = new HashSet<>();

            // when
            option.addContent(ids, contents);

            // then
            assertThat(contents).isEmpty();
        }
    }
}
