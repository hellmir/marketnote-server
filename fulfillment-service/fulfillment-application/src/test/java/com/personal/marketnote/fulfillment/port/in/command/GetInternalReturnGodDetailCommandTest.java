package com.personal.marketnote.fulfillment.port.in.command;

import com.personal.marketnote.fulfillment.exception.InvalidReturnSlipNumbersCountException;
import com.personal.marketnote.fulfillment.exception.ReturnSlipNumbersNoValueException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("GetInternalReturnGodDetailCommand 팩토리 메서드 테스트")
class GetInternalReturnGodDetailCommandTest {

    @Nested
    @DisplayName("of() 팩토리 메서드")
    class Of {

        @Test
        @DisplayName("returnSlipNumbers가 null이면 ReturnSlipNumbersNoValueException이 발생한다")
        void throwsWhenNull() {
            assertThatThrownBy(() -> GetInternalReturnGodDetailCommand.of(null))
                    .isInstanceOf(ReturnSlipNumbersNoValueException.class)
                    .hasMessageContaining("returnSlipNumbers는 필수입니다.");
        }

        @Test
        @DisplayName("returnSlipNumbers가 빈 문자열이면 ReturnSlipNumbersNoValueException이 발생한다")
        void throwsWhenBlank() {
            assertThatThrownBy(() -> GetInternalReturnGodDetailCommand.of(""))
                    .isInstanceOf(ReturnSlipNumbersNoValueException.class);
        }

        @Test
        @DisplayName("returnSlipNumbers 개수가 100을 초과하면 InvalidReturnSlipNumbersCountException이 발생한다")
        void throwsWhenCountExceedsMax() {
            String slips = IntStream.rangeClosed(1, 101)
                    .mapToObj(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElseThrow();

            assertThatThrownBy(() -> GetInternalReturnGodDetailCommand.of(slips))
                    .isInstanceOf(InvalidReturnSlipNumbersCountException.class)
                    .hasMessageContaining("100")
                    .hasMessageContaining("101");
        }

        @Test
        @DisplayName("returnSlipNumbers 개수가 정확히 100이면 정상 생성된다")
        void succeedsWhenCountEqualsMax() {
            String slips = IntStream.rangeClosed(1, 100)
                    .mapToObj(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElseThrow();

            GetInternalReturnGodDetailCommand command = GetInternalReturnGodDetailCommand.of(slips);

            assertThat(command.returnSlipNumbers()).isEqualTo(slips);
        }

        @Test
        @DisplayName("단일 returnSlipNumber 입력 시 정상 생성된다")
        void succeedsWithSingleSlip() {
            GetInternalReturnGodDetailCommand command = GetInternalReturnGodDetailCommand.of("R-001");

            assertThat(command.returnSlipNumbers()).isEqualTo("R-001");
        }

        @Test
        @DisplayName("쉼표 구분 여러 returnSlipNumbers 입력 시 정상 생성된다")
        void succeedsWithMultipleSlips() {
            GetInternalReturnGodDetailCommand command = GetInternalReturnGodDetailCommand.of("R-001,R-002,R-003");

            assertThat(command.returnSlipNumbers()).isEqualTo("R-001,R-002,R-003");
        }
    }
}
