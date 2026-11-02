package com.personal.marketnote.common.configuration.swagger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerEnvironmentTest {

    @Test
    @DisplayName("prod 단일 프로필이면 Live 라벨을 반환한다")
    void resolvesProdToLive() {
        String label = SwaggerEnvironment.resolveLabel(new String[]{"prod"});
        assertThat(label).isEqualTo("Live");
    }

    @Test
    @DisplayName("qa.test 단일 프로필이면 QA 라벨을 반환한다")
    void resolvesQaTestToQa() {
        String label = SwaggerEnvironment.resolveLabel(new String[]{"qa.test"});
        assertThat(label).isEqualTo("QA");
    }

    @Test
    @DisplayName("prod 와 다른 프로필이 함께 있으면 Live가 우선한다")
    void prodHasPriorityOverOtherProfiles() {
        String label = SwaggerEnvironment.resolveLabel(new String[]{"kafka", "prod"});
        assertThat(label).isEqualTo("Live");
    }

    @Test
    @DisplayName("qa.test 와 다른 프로필이 함께 있으면 QA가 우선한다")
    void qaTestHasPriorityOverOtherProfiles() {
        String label = SwaggerEnvironment.resolveLabel(new String[]{"kafka", "qa.test"});
        assertThat(label).isEqualTo("QA");
    }

    @Test
    @DisplayName("prod 와 qa.test 가 동시에 있으면 Live가 우선한다")
    void prodHasPriorityOverQaTest() {
        String label = SwaggerEnvironment.resolveLabel(new String[]{"qa.test", "prod"});
        assertThat(label).isEqualTo("Live");
    }

    @Test
    @DisplayName("prod와 qa.test 둘 다 없으면 첫 번째 활성 프로필명을 라벨로 반환한다")
    void usesFirstProfileWhenNoKnownProfile() {
        String label = SwaggerEnvironment.resolveLabel(new String[]{"local"});
        assertThat(label).isEqualTo("local");
    }

    @Test
    @DisplayName("활성 프로필이 비어 있으면 빈 문자열을 반환한다")
    void returnsEmptyLabelWhenProfilesAreEmpty() {
        String label = SwaggerEnvironment.resolveLabel(new String[0]);
        assertThat(label).isEmpty();
    }

    @Test
    @DisplayName("활성 프로필이 null이면 빈 문자열을 반환한다")
    void returnsEmptyLabelWhenProfilesIsNull() {
        String label = SwaggerEnvironment.resolveLabel(null);
        assertThat(label).isEmpty();
    }
}
