package com.personal.marketnote.user.domain.remotearea;

import com.personal.marketnote.user.domain.remotearea.exception.*;
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
        @DisplayName("광역시도만 지정하여 RemoteArea를 생성한다")
        void shouldCreateRemoteAreaWithProvinceOnly() {
            // given
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .province("인천")
                    .build();

            // when
            RemoteArea remoteArea = RemoteArea.from(state);

            // then
            assertThat(remoteArea.getProvince()).isEqualTo("인천");
            assertThat(remoteArea.getDistrict()).isEmpty();
            assertThat(remoteArea.getVillage()).isEmpty();
            assertThat(remoteArea.getSubarea()).isEmpty();
            assertThat(remoteArea.isActive()).isTrue();
        }

        @Test
        @DisplayName("광역시도, 시군구, 읍면동을 지정하여 RemoteArea를 생성한다")
        void shouldCreateRemoteAreaWithProvinceDistrictVillage() {
            // given
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .province("경남")
                    .district("통영시")
                    .village("사량면")
                    .build();

            // when
            RemoteArea remoteArea = RemoteArea.from(state);

            // then
            assertThat(remoteArea.getProvince()).isEqualTo("경남");
            assertThat(remoteArea.getDistrict()).isEqualTo("통영시");
            assertThat(remoteArea.getVillage()).isEqualTo("사량면");
            assertThat(remoteArea.getSubarea()).isEmpty();
        }

        @Test
        @DisplayName("모든 필드를 지정하여 RemoteArea를 생성한다")
        void shouldCreateRemoteAreaWithAllFields() {
            // given
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .province("충남")
                    .district("보령시")
                    .village("오천면")
                    .subarea("녹도리")
                    .build();

            // when
            RemoteArea remoteArea = RemoteArea.from(state);

            // then
            assertThat(remoteArea.getProvince()).isEqualTo("충남");
            assertThat(remoteArea.getDistrict()).isEqualTo("보령시");
            assertThat(remoteArea.getVillage()).isEqualTo("오천면");
            assertThat(remoteArea.getSubarea()).isEqualTo("녹도리");
        }

        @Test
        @DisplayName("광역시도가 null이면 RemoteAreaProvinceNoValueException이 발생한다")
        void shouldThrowWhenProvinceIsNull() {
            // given
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .province(null)
                    .district("옹진군")
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(RemoteAreaProvinceNoValueException.class);
        }

        @Test
        @DisplayName("광역시도가 빈 문자열이면 RemoteAreaProvinceNoValueException이 발생한다")
        void shouldThrowWhenProvinceIsBlank() {
            // given
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .province("   ")
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(RemoteAreaProvinceNoValueException.class);
        }

        @Test
        @DisplayName("광역시도가 50자를 초과하면 InvalidRemoteAreaProvinceLengthException이 발생한다")
        void shouldThrowWhenProvinceExceeds50Characters() {
            // given
            String longProvince = "가".repeat(51);
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .province(longProvince)
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(InvalidRemoteAreaProvinceLengthException.class);
        }

        @Test
        @DisplayName("시군구가 50자를 초과하면 InvalidRemoteAreaDistrictLengthException이 발생한다")
        void shouldThrowWhenDistrictExceeds50Characters() {
            // given
            String longDistrict = "가".repeat(51);
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .province("충남")
                    .district(longDistrict)
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(InvalidRemoteAreaDistrictLengthException.class);
        }

        @Test
        @DisplayName("읍면동이 50자를 초과하면 InvalidRemoteAreaVillageLengthException이 발생한다")
        void shouldThrowWhenVillageExceeds50Characters() {
            // given
            String longVillage = "가".repeat(51);
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .province("충남")
                    .district("보령시")
                    .village(longVillage)
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(InvalidRemoteAreaVillageLengthException.class);
        }

        @Test
        @DisplayName("세부지역이 50자를 초과하면 InvalidRemoteAreaSubareaLengthException이 발생한다")
        void shouldThrowWhenSubareaExceeds50Characters() {
            // given
            String longSubarea = "가".repeat(51);
            RemoteAreaCreateState state = RemoteAreaCreateState.builder()
                    .province("충남")
                    .district("보령시")
                    .village("오천면")
                    .subarea(longSubarea)
                    .build();

            // when & then
            assertThatThrownBy(() -> RemoteArea.from(state))
                    .isInstanceOf(InvalidRemoteAreaSubareaLengthException.class);
        }
    }

    @Nested
    @DisplayName("deactivate")
    class Deactivate {

        @Test
        @DisplayName("deactivate를 호출하면 상태가 INACTIVE로 변경된다")
        void shouldChangeStatusToInactive() {
            // given
            RemoteArea remoteArea = RemoteArea.from(
                    RemoteAreaSnapshotState.builder()
                            .id(1L)
                            .province("충남")
                            .district("보령시")
                            .village("오천면")
                            .subarea("녹도리")
                            .build()
            );

            // when
            remoteArea.deactivate();

            // then
            assertThat(remoteArea.isInactive()).isTrue();
            assertThat(remoteArea.isActive()).isFalse();
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
                    .province("충남")
                    .district("보령시")
                    .village("오천면")
                    .subarea("녹도리")
                    .build();

            // when
            RemoteArea remoteArea = RemoteArea.from(state);

            // then
            assertThat(remoteArea.getId()).isEqualTo(1L);
            assertThat(remoteArea.getProvince()).isEqualTo("충남");
            assertThat(remoteArea.getDistrict()).isEqualTo("보령시");
            assertThat(remoteArea.getVillage()).isEqualTo("오천면");
            assertThat(remoteArea.getSubarea()).isEqualTo("녹도리");
        }
    }
}
