package com.personal.marketnote.product.adapter.in.web.option.response;

import com.personal.marketnote.product.port.in.result.option.SelectableProductOptionItemResult;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class SelectableProductOptionResponse {
    private Long id;
    private String content;
    private String status;
    private Boolean isSelected;

    public static SelectableProductOptionResponse from(SelectableProductOptionItemResult result) {
        return SelectableProductOptionResponse.builder()
                .id(result.id())
                .content(result.content())
                .status(result.status())
                .isSelected(result.isSelected())
                .build();
    }
}


