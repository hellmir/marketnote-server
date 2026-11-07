package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.notification.domain.aligo.BrandTalkWideItem;
import com.personal.marketnote.notification.port.in.command.BrandTalkWideItemCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrandTalkWideItemCommandToStateMapperTest {

    @Test
    @DisplayName("와이드 아이템 Command는 도메인으로 매핑된다")
    void mapsWideItemCommand() {
        BrandTalkWideItemCommand command = new BrandTalkWideItemCommand(
                "타이틀", "https://img.example.com/1.png",
                "https://m.example.com", "https://pc.example.com", null, null
        );

        BrandTalkWideItem wideItem = BrandTalkWideItemCommandToStateMapper.mapWideItem(command);

        assertThat(wideItem).isNotNull();
        assertThat(wideItem.getTitle()).isEqualTo("타이틀");
        assertThat(wideItem.getImageUrl()).isEqualTo("https://img.example.com/1.png");
        assertThat(wideItem.getLinkM()).isEqualTo("https://m.example.com");
        assertThat(wideItem.getLinkP()).isEqualTo("https://pc.example.com");
    }

    @Test
    @DisplayName("와이드 아이템 Command가 null이면 null을 반환한다")
    void returnsNullWhenCommandNull() {
        BrandTalkWideItem wideItem = BrandTalkWideItemCommandToStateMapper.mapWideItem(null);

        assertThat(wideItem).isNull();
    }

    @Test
    @DisplayName("와이드 아이템 Command 리스트는 도메인 리스트로 매핑된다")
    void mapsWideItemCommandList() {
        List<BrandTalkWideItemCommand> commands = List.of(
                new BrandTalkWideItemCommand("A", null, null, null, null, null),
                new BrandTalkWideItemCommand("B", null, null, null, null, null)
        );

        List<BrandTalkWideItem> items = BrandTalkWideItemCommandToStateMapper.mapWideItems(commands);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getTitle()).isEqualTo("A");
        assertThat(items.get(1).getTitle()).isEqualTo("B");
    }

    @Test
    @DisplayName("와이드 아이템 Command 리스트가 null이면 빈 리스트를 반환한다")
    void returnsEmptyListWhenCommandsNull() {
        List<BrandTalkWideItem> items = BrandTalkWideItemCommandToStateMapper.mapWideItems(null);

        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("mapWideItems는 빈 리스트 입력 시 빈 리스트를 반환한다")
    void returnsEmptyListWhenCommandsEmpty() {
        List<BrandTalkWideItem> items = BrandTalkWideItemCommandToStateMapper.mapWideItems(List.of());

        assertThat(items).isEmpty();
    }
}
