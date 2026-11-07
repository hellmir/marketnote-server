package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.notification.domain.aligo.AligoTemplateButtonLinkType;
import com.personal.marketnote.notification.domain.aligo.BrandTalkButton;
import com.personal.marketnote.notification.port.in.command.BrandTalkButtonCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrandTalkButtonCommandToStateMapperTest {

    @Test
    @DisplayName("WL 타입 버튼 Command는 도메인으로 매핑된다")
    void mapsWebLinkButtonCommand() {
        BrandTalkButtonCommand command = new BrandTalkButtonCommand(
                "자세히 보기", "WL", "https://m.example.com", "https://pc.example.com", null, null
        );

        BrandTalkButton button = BrandTalkButtonCommandToStateMapper.mapButton(command);

        assertThat(button.getName()).isEqualTo("자세히 보기");
        assertThat(button.getLinkType()).isEqualTo(AligoTemplateButtonLinkType.WL);
        assertThat(button.getLinkM()).isEqualTo("https://m.example.com");
        assertThat(button.getLinkP()).isEqualTo("https://pc.example.com");
    }

    @Test
    @DisplayName("버튼 Command 리스트는 도메인 리스트로 매핑된다")
    void mapsButtonCommandList() {
        List<BrandTalkButtonCommand> commands = List.of(
                new BrandTalkButtonCommand("버튼A", "AC", null, null, null, null),
                new BrandTalkButtonCommand("버튼B", "AC", null, null, null, null)
        );

        List<BrandTalkButton> buttons = BrandTalkButtonCommandToStateMapper.mapButtons(commands);

        assertThat(buttons).hasSize(2);
        assertThat(buttons.get(0).getName()).isEqualTo("버튼A");
        assertThat(buttons.get(1).getName()).isEqualTo("버튼B");
    }

    @Test
    @DisplayName("버튼 Command 리스트가 null이면 빈 리스트를 반환한다")
    void returnsEmptyListWhenCommandsNull() {
        List<BrandTalkButton> buttons = BrandTalkButtonCommandToStateMapper.mapButtons(null);

        assertThat(buttons).isEmpty();
    }

    @Test
    @DisplayName("버튼 Command 리스트가 비어 있으면 빈 리스트를 반환한다")
    void returnsEmptyListWhenCommandsEmpty() {
        List<BrandTalkButton> buttons = BrandTalkButtonCommandToStateMapper.mapButtons(List.of());

        assertThat(buttons).isEmpty();
    }
}
