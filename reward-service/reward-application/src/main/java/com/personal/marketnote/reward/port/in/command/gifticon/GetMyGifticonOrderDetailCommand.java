package com.personal.marketnote.reward.port.in.command.gifticon;

public record GetMyGifticonOrderDetailCommand(
        Long userId,
        Long orderId
) {
}
