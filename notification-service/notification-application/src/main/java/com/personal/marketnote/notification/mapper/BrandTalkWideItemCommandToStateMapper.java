package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.notification.domain.aligo.BrandTalkWideItem;
import com.personal.marketnote.notification.domain.aligo.BrandTalkWideItemCreateState;
import com.personal.marketnote.notification.port.in.command.BrandTalkWideItemCommand;

import java.util.List;

public class BrandTalkWideItemCommandToStateMapper {

    private BrandTalkWideItemCommandToStateMapper() {
    }

    public static BrandTalkWideItem mapWideItem(BrandTalkWideItemCommand command) {
        if (FormatValidator.hasNoValue(command)) {
            return null;
        }
        return BrandTalkWideItem.from(
                BrandTalkWideItemCreateState.builder()
                        .title(command.title())
                        .imageUrl(command.imageUrl())
                        .linkM(command.linkM())
                        .linkP(command.linkP())
                        .linkI(command.linkI())
                        .linkA(command.linkA())
                        .build()
        );
    }

    public static List<BrandTalkWideItem> mapWideItems(List<BrandTalkWideItemCommand> commands) {
        if (FormatValidator.hasNoValue(commands)) {
            return List.of();
        }
        return commands.stream()
                .map(BrandTalkWideItemCommandToStateMapper::mapWideItem)
                .toList();
    }
}
