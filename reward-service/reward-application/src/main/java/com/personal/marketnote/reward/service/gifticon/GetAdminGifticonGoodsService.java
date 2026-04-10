package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.common.utility.FormatValidator;
import com.personal.marketnote.reward.port.in.command.gifticon.GetAdminGifticonGoodsCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetAdminGifticonGoodsResult;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetAdminGifticonGoodsUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonGoodsPort.FindAllForAdminResult;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
@Transactional(isolation = READ_COMMITTED, readOnly = true)
public class GetAdminGifticonGoodsService implements GetAdminGifticonGoodsUseCase {

    private final FindGifticonGoodsPort findGifticonGoodsPort;

    @Override
    public GetAdminGifticonGoodsResult getAdminGifticonGoods(GetAdminGifticonGoodsCommand command) {
        String goodsStatus = FormatValidator.hasValue(command.goodsStatus()) ? command.goodsStatus() : "";
        String keyword = FormatValidator.hasValue(command.keyword()) ? command.keyword() : "";

        FindAllForAdminResult portResult = findGifticonGoodsPort.findAllForAdmin(
                command.page(),
                command.pageSize(),
                goodsStatus,
                command.exposed(),
                keyword
        );

        return GetAdminGifticonGoodsResult.from(command.page(), command.pageSize(), portResult);
    }
}
