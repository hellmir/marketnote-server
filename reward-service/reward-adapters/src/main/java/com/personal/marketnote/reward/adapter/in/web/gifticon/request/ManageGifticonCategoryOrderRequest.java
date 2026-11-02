package com.personal.marketnote.reward.adapter.in.web.gifticon.request;

import com.personal.marketnote.reward.port.in.command.gifticon.ManageGifticonCategoryOrderCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ManageGifticonCategoryOrderRequest {
    @NotEmpty
    @Valid
    private List<OrderItem> items;

    @Getter
    @NoArgsConstructor
    public static class OrderItem {
        @NotNull
        private Long categoryId;
        @NotNull
        private Integer orderNum;
    }

    public ManageGifticonCategoryOrderCommand toCommand() {
        List<ManageGifticonCategoryOrderCommand.OrderItem> commandItems = items.stream()
                .map(item -> new ManageGifticonCategoryOrderCommand.OrderItem(
                        item.getCategoryId(), item.getOrderNum()
                ))
                .toList();
        return new ManageGifticonCategoryOrderCommand(commandItems);
    }
}
