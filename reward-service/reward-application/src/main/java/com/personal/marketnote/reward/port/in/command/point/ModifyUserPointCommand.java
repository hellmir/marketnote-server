package com.personal.marketnote.reward.port.in.command.point;

import com.personal.marketnote.reward.domain.offerwall.OfferwallMapper;
import com.personal.marketnote.reward.domain.point.ReferralBonusTier;
import com.personal.marketnote.reward.domain.point.UserPointChangeType;
import com.personal.marketnote.reward.domain.point.UserPointSourceType;
import com.personal.marketnote.reward.port.in.command.offerwall.RegisterOfferwallRewardCommand;
import lombok.Builder;

@Builder
public record ModifyUserPointCommand(
        Long userId,
        String userKey,
        UserPointChangeType changeType,
        Long amount,
        UserPointSourceType sourceType,
        Long sourceId,
        String reason
) {
    public boolean isAccrual() {
        return changeType.isAccrual();
    }

    public static ModifyUserPointCommand forGifticonPurchase(Long userId, String goodsName, Long cashPrice) {
        return ModifyUserPointCommand.builder()
                .userId(userId)
                .changeType(UserPointChangeType.DEDUCTION)
                .amount(cashPrice)
                .sourceType(UserPointSourceType.GIFTICON_PURCHASE)
                .reason("기프티콘 구매: " + goodsName)
                .build();
    }

    public static ModifyUserPointCommand forGifticonRefund(Long userId, String goodsName, Long cashPrice) {
        return ModifyUserPointCommand.builder()
                .userId(userId)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount(cashPrice)
                .sourceType(UserPointSourceType.GIFTICON_REFUND)
                .reason("기프티콘 구매 실패 환불: " + goodsName)
                .build();
    }

    public static ModifyUserPointCommand forReferralBonus(Long userId, ReferralBonusTier tier) {
        return ModifyUserPointCommand.builder()
                .userId(userId)
                .changeType(UserPointChangeType.ACCRUAL)
                .amount((long) tier.getBonusAmount())
                .sourceType(UserPointSourceType.USER)
                .sourceId(userId)
                .reason(tier.getReason())
                .build();
    }

    public static ModifyUserPointCommand forOfferwallReward(
            RegisterOfferwallRewardCommand command, OfferwallMapper offerwallMapper
    ) {
        return ModifyUserPointCommand.builder()
                .userKey(command.userKey())
                .changeType(UserPointChangeType.ACCRUAL)
                .amount(command.quantity())
                .sourceType(UserPointSourceType.OFFERWALL)
                .sourceId(offerwallMapper.getId())
                .reason(String.format("%s 리워드 보상 적립", command.offerwallType().getDescription()))
                .build();
    }
}
