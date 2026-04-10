package com.personal.marketnote.reward.adapter.in.web.gifticon.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.personal.marketnote.reward.port.in.command.gifticon.UpdateGifticonCategoryCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateGifticonCategoryRequest {
    private String displayName;
    private boolean displayNameProvided;
    private String iconUrl;
    private boolean iconUrlProvided;

    @JsonSetter(value = "displayName", nulls = Nulls.SET)
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
        this.displayNameProvided = true;
    }

    @JsonSetter(value = "iconUrl", nulls = Nulls.SET)
    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        this.iconUrlProvided = true;
    }

    public UpdateGifticonCategoryCommand toCommand(Long categoryId) {
        return new UpdateGifticonCategoryCommand(
                categoryId,
                displayName,
                displayNameProvided,
                iconUrl,
                iconUrlProvided
        );
    }
}
