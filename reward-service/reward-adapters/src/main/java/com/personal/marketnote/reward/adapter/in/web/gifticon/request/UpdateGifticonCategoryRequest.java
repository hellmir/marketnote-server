package com.personal.marketnote.reward.adapter.in.web.gifticon.request;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.personal.marketnote.reward.port.in.command.gifticon.UpdateGifticonCategoryCommand;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateGifticonCategoryRequest {
    @Size(max = 50, message = "displayName은 50자 이하여야 합니다.")
    private String displayName;
    private boolean displayNameProvided;

    @Size(max = 2048, message = "iconUrl은 2048자 이하여야 합니다.")
    @Pattern(regexp = "^https://.+", message = "iconUrl은 https://로 시작해야 합니다.")
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
