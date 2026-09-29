package com.personal.marketnote.common.adapter.out.persistence.audit;

import com.personal.marketnote.common.domain.EntityStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BaseGeneralEntityTest {

    @Test
    @DisplayName("엔티티 생성 시 기본 상태는 ACTIVE이다")
    void shouldHaveActiveStatusOnCreation() {
        // when
        TestEntity entity = new TestEntity();

        // then
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("activate 호출 시 상태가 ACTIVE로 변경된다")
    void shouldChangeStatusToActiveWhenActivated() {
        // given
        TestEntity entity = new TestEntity();
        entity.deactivateForTest();

        // when
        entity.activateForTest();

        // then
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("deactivate 호출 시 상태가 INACTIVE로 변경된다")
    void shouldChangeStatusToInactiveWhenDeactivated() {
        // given
        TestEntity entity = new TestEntity();

        // when
        entity.deactivateForTest();

        // then
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);
    }

    @Test
    @DisplayName("hide 호출 시 상태가 UNEXPOSED로 변경된다")
    void shouldChangeStatusToUnexposedWhenHidden() {
        // given
        TestEntity entity = new TestEntity();

        // when
        entity.hideForTest();

        // then
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);
    }

    @Test
    @DisplayName("INACTIVE 상태에서 activate 호출 시 ACTIVE로 전이된다")
    void shouldTransitionFromInactiveToActive() {
        // given
        TestEntity entity = new TestEntity();
        entity.deactivateForTest();
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.INACTIVE);

        // when
        entity.activateForTest();

        // then
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    @Test
    @DisplayName("UNEXPOSED 상태에서 activate 호출 시 ACTIVE로 전이된다")
    void shouldTransitionFromUnexposedToActive() {
        // given
        TestEntity entity = new TestEntity();
        entity.hideForTest();
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.UNEXPOSED);

        // when
        entity.activateForTest();

        // then
        assertThat(entity.getStatus()).isEqualTo(EntityStatus.ACTIVE);
    }

    private static class TestEntity extends BaseGeneralEntity {
        public void activateForTest() {
            activate();
        }

        public void deactivateForTest() {
            deactivate();
        }

        public void hideForTest() {
            hide();
        }
    }
}
