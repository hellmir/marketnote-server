package com.personal.marketnote.reward.port.in.command.gifticon;

import java.util.List;

public record ManageGifticonGoodsCashPriceCommand(List<CashPriceItem> items) {
    public record CashPriceItem(String goodsCode, Long cashPrice) {
    }
}
