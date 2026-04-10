package com.personal.marketnote.reward.adapter.in.web.gifticon.request;

import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryExposureCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ManageGifticonCategoryExposureRequest {
    @NotEmpty
    @Valid
    private List<ExposureItem> items;

    @Getter
    @NoArgsConstructor
    public static class ExposureItem {
        @NotNull
        private Long categoryId;
        @NotNull
        private Boolean exposed;
    }

    public ManageGifticonCategoryExposureCommand toCommand() {
        List<ManageGifticonCategoryExposureCommand.ExposureItem> commandItems = items.stream()
                .map(item -> new ManageGifticonCategoryExposureCommand.ExposureItem(
                        item.getCategoryId(), item.getExposed()
                ))
                .toList();
        return new ManageGifticonCategoryExposureCommand(commandItems);
    }
}
