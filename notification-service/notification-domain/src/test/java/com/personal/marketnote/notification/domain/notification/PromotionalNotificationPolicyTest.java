package com.personal.marketnote.notification.domain.notification;

import com.personal.marketnote.notification.domain.template.NotificationCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionalNotificationPolicyTest {

    @Nested
    @DisplayName("applyAdLabel 메서드")
    class ApplyAdLabel {

        @Test
        @DisplayName("PROMOTIONAL 알림 제목 앞에 (광고) 접두사를 삽입한다")
        void shouldPrependAdLabelToTitle() {
            // given
            String title = "이벤트 안내";

            // when
            String result = PromotionalNotificationPolicy.applyAdLabel(title);

            // then
            assertThat(result).isEqualTo("(광고) 이벤트 안내");
        }

        @Test
        @DisplayName("이미 (광고) 접두사가 있으면 중복 삽입하지 않는다")
        void shouldNotDuplicateAdLabel() {
            // given
            String title = "(광고) 이벤트 안내";

            // when
            String result = PromotionalNotificationPolicy.applyAdLabel(title);

            // then
            assertThat(result).isEqualTo("(광고) 이벤트 안내");
        }
    }

    @Nested
    @DisplayName("applyOptOutGuide 메서드")
    class ApplyOptOutGuide {

        @Test
        @DisplayName("PROMOTIONAL 알림 본문 끝에 수신 거부 안내를 삽입한다")
        void shouldAppendOptOutGuideToBody() {
            // given
            String body = "특별 할인 이벤트가 진행 중입니다.";

            // when
            String result = PromotionalNotificationPolicy.applyOptOutGuide(body);

            // then
            assertThat(result).isEqualTo("특별 할인 이벤트가 진행 중입니다.\n[수신거부:더보기>설정]");
        }

        @Test
        @DisplayName("이미 수신 거부 안내가 있으면 중복 삽입하지 않는다")
        void shouldNotDuplicateOptOutGuide() {
            // given
            String body = "특별 할인 이벤트가 진행 중입니다.\n[수신거부:더보기>설정]";

            // when
            String result = PromotionalNotificationPolicy.applyOptOutGuide(body);

            // then
            assertThat(result).isEqualTo("특별 할인 이벤트가 진행 중입니다.\n[수신거부:더보기>설정]");
        }
    }

    @Nested
    @DisplayName("shouldApplyPolicy 메서드")
    class ShouldApplyPolicy {

        @Test
        @DisplayName("PROMOTIONAL 카테고리이면 정책을 적용한다")
        void shouldApplyForPromotional() {
            assertThat(PromotionalNotificationPolicy.shouldApplyPolicy(NotificationCategory.PROMOTIONAL)).isTrue();
        }

        @Test
        @DisplayName("MANDATORY 카테고리이면 정책을 적용하지 않는다")
        void shouldNotApplyForMandatory() {
            assertThat(PromotionalNotificationPolicy.shouldApplyPolicy(NotificationCategory.MANDATORY)).isFalse();
        }

        @Test
        @DisplayName("INFORMATIONAL 카테고리이면 정책을 적용하지 않는다")
        void shouldNotApplyForInformational() {
            assertThat(PromotionalNotificationPolicy.shouldApplyPolicy(NotificationCategory.INFORMATIONAL)).isFalse();
        }
    }
}
