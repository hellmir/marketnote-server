package com.personal.marketnote.reward.port.in.command.gifticon;

public record GetMyGifticonOrdersCommand(
        Long userId,
        String statusFilter,
        String sortType,
        Long cursor,
        int pageSize
) {
}
