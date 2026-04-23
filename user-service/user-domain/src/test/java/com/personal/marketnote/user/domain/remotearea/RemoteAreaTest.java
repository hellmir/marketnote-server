package com.personal.marketnote.user.domain.remotearea;

import com.personal.marketnote.user.domain.remotearea.exception.InvalidRemoteAreaRegionNameLengthException;
import com.personal.marketnote.user.domain.remotearea.exception.InvalidRemoteAreaZipCodeException;
import com.personal.marketnote.user.domain.remotearea.exception.RemoteAreaRegionNameNoValueException;
import com.personal.marketnote.user.domain.remotearea.exception.RemoteAreaTypeNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RemoteAreaTest {

    @Nested
    @DisplayName("from(CreateState)")
    class FromCreateState {

        @Test
        @DisplayName("유효한 CreateState로 RemoteArea를 생성한다")
        void shouldCreateRemoteAreaWithValidState() {
            // given
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .zipCode("63000")
                    .remoteAreaType(RemoteAreaType.JEJU)
                    .regionName("제주특별자치도 제주시")
                    .build();

            // when
            RemoteArea remoteArea = RemoteArea.from(state);

            // then
            assertThat(remoteArea.getZipCode()).isEqualTo("63000");
            assertThat(remoteArea.getRemoteAreaType()).isEqualTo(RemoteAreaType.JEJU);
            assertThat(remoteArea.getRegionName()).isEqualTo("제주특별자치도 제주시");
            assertThat(remoteArea.isActive()).isTrue();
        }

        @Test
        @DisplayName("우편번호가 null이면 InvalidRemoteAreaZipCodeException이 발생한다")
        void shouldThrowWhenZipCodeIsNull() {
            // given
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .zipCode(null)
                    .remoteAreaType(RemoteAreaType.JEJU)
                    .regionName("제주특별자치도 제주시")
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(InvalidRemoteAreaZipCodeException.class);
        }

        @Test
        @DisplayName("우편번호가 5자리가 아니면 InvalidRemoteAreaZipCodeException이 발생한다")
        void shouldThrowWhenZipCodeIsNot5Digits() {
            // given
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .zipCode("1234")
                    .remoteAreaType(RemoteAreaType.ISLAND_MOUNTAINOUS)
                    .regionName("경남 통영시 한산면")
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(InvalidRemoteAreaZipCodeException.class);
        }

        @Test
        @DisplayName("우편번호에 숫자가 아닌 문자가 포함되면 InvalidRemoteAreaZipCodeException이 발생한다")
        void shouldThrowWhenZipCodeContainsNonDigit() {
            // given
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .zipCode("6300a")
                    .remoteAreaType(RemoteAreaType.JEJU)
                    .regionName("제주특별자치도 제주시")
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(InvalidRemoteAreaZipCodeException.class);
        }

        @Test
        @DisplayName("지역 유형이 null이면 RemoteAreaTypeNoValueException이 발생한다")
        void shouldThrowWhenRemoteAreaTypeIsNull() {
            // given
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .zipCode("63000")
                    .remoteAreaType(null)
                    .regionName("제주특별자치도 제주시")
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(RemoteAreaTypeNoValueException.class);
        }

        @Test
        @DisplayName("지역명이 null이면 RemoteAreaRegionNameNoValueException이 발생한다")
        void shouldThrowWhenRegionNameIsNull() {
            // given
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .zipCode("63000")
                    .remoteAreaType(RemoteAreaType.JEJU)
                    .regionName(null)
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(RemoteAreaRegionNameNoValueException.class);
        }

        @Test
        @DisplayName("지역명이 빈 문자열이면 RemoteAreaRegionNameNoValueException이 발생한다")
        void shouldThrowWhenRegionNameIsBlank() {
            // given
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .zipCode("63000")
                    .remoteAreaType(RemoteAreaType.JEJU)
                    .regionName("   ")
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(RemoteAreaRegionNameNoValueException.class);
        }

        @Test
        @DisplayName("지역명이 100자를 초과하면 InvalidRemoteAreaRegionNameLengthException이 발생한다")
        void shouldThrowWhenRegionNameExceeds100Characters() {
            // given
            String longRegionName = "가".repeat(101);
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .zipCode("63000")
                    .remoteAreaType(RemoteAreaType.JEJU)
                    .regionName(longRegionName)
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(InvalidRemoteAreaRegionNameLengthException.class);
        }
    }

    @Nested
    @DisplayName("from(SnapshotState)")
    class FromSnapshotState {

        @Test
        @DisplayName("SnapshotState로 RemoteArea를 복원한다")
        void shouldRestoreRemoteAreaFromSnapshotState() {
            // given
            RemoteAreaSnapshotState state = RemoteAreaSnapshotState.builder()
                    .id(1L)
                    .zipCode("63000")
                    .remoteAreaType(RemoteAreaType.JEJU)
                    .regionName("제주특별자치도 제주시")
                    .build();

            // when
            RemoteArea remoteArea = RemoteArea.from(state);

            // then
            assertThat(remoteArea.getId()).isEqualTo(1L);
            assertThat(remoteArea.getZipCode()).isEqualTo("63000");
            assertThat(remoteArea.getRemoteAreaType()).isEqualTo(RemoteAreaType.JEJU);
            assertThat(remoteArea.getRegionName()).isEqualTo("제주특별자치도 제주시");
        }
    }

    @Nested
    @DisplayName("술어 메서드")
    class PredicateMethods {

        @Test
        @DisplayName("제주 지역이면 isJeju()가 true를 반환한다")
        void shouldReturnTrueForJeju() {
            // given
            RemoteArea remoteArea = RemoteArea.from(
                    RemoteAreaSnapshotState.builder()
                            .id(1L)
                            .zipCode("63000")
                            .remoteAreaType(RemoteAreaType.JEJU)
                            .regionName("제주특별자치도 제주시")
                            .build()
            );

            // when & then
            assertThat(remoteArea.isJeju()).isTrue();
            assertThat(remoteArea.isIslandMountainous()).isFalse();
        }

        @Test
        @DisplayName("도서산간 지역이면 isIslandMountainous()가 true를 반환한다")
        void shouldReturnTrueForIslandMountainous() {
            // given
            RemoteArea remoteArea = RemoteArea.from(
                    RemoteAreaSnapshotState.builder()
                            .id(2L)
                            .zipCode("53000")
                            .remoteAreaType(RemoteAreaType.ISLAND_MOUNTAINOUS)
                            .regionName("경남 통영시 한산면")
                            .build()
            );

            // when & then
            assertThat(remoteArea.isIslandMountainous()).isTrue();
            assertThat(remoteArea.isJeju()).isFalse();
        }
    }
}
