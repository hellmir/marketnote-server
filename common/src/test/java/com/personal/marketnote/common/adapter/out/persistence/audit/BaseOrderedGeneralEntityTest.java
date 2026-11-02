package com.personal.marketnote.common.adapter.out.persistence.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class BaseOrderedGeneralEntityTest {

    @Test
    @DisplayName("생성 직후 orderNum은 null이다")
    void shouldHaveNullOrderNumOnCreation() {
        // when
        TestOrderedEntity entity = new TestOrderedEntity();

        // then
        assertThat(entity.getOrderNum()).isNull();
    }

    @Test
    @DisplayName("setIdToOrderNum 호출 시 orderNum이 id와 동일하게 설정된다")
    void shouldSetOrderNumToIdWhenSetIdToOrderNumCalled() {
        // given
        TestOrderedEntity entity = new TestOrderedEntity();
        ReflectionTestUtils.setField(entity, "id", 42L);

        // when
        entity.setIdToOrderNum();

        // then
        assertThat(entity.getOrderNum()).isEqualTo(42L);
    }

    @Test
    @DisplayName("updateOrderNum 호출 시 orderNum이 지정된 값으로 변경된다")
    void shouldUpdateOrderNumToSpecifiedValue() {
        // given
        TestOrderedEntity entity = new TestOrderedEntity();

        // when
        entity.updateOrderNumForTest(100L);

        // then
        assertThat(entity.getOrderNum()).isEqualTo(100L);
    }

    @Test
    @DisplayName("id가 null일 때 setIdToOrderNum 호출 시 orderNum도 null이다")
    void shouldSetOrderNumToNullWhenIdIsNull() {
        // given
        TestOrderedEntity entity = new TestOrderedEntity();

        // when
        entity.setIdToOrderNum();

        // then
        assertThat(entity.getOrderNum()).isNull();
    }

    private static class TestOrderedEntity extends BaseOrderedGeneralEntity {
        public void updateOrderNumForTest(Long orderNum) {
            updateOrderNum(orderNum);
        }
    }
}
