package com.personal.marketnote.reward.service.gifticon;

import com.personal.marketnote.common.application.UseCase;
import com.personal.marketnote.reward.domain.gifticon.*;
import com.personal.marketnote.reward.port.in.command.gifticon.GetMyGifticonOrdersCommand;
import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrdersResult;
import com.personal.marketnote.reward.port.in.result.gifticon.GetMyGifticonOrdersResult.MyGifticonOrderItem;
import com.personal.marketnote.reward.port.in.usecase.gifticon.GetMyGifticonOrdersUseCase;
import com.personal.marketnote.reward.port.out.gifticon.FindGifticonOrderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.springframework.transaction.annotation.Isolation.READ_COMMITTED;

@UseCase
@RequiredArgsConstructor
public class GetMyGifticonOrdersService implements GetMyGifticonOrdersUseCase {

    private final FindGifticonOrderPort findGifticonOrderPort;
    private final Clock clock;

    @Override
    @Transactional(isolation = READ_COMMITTED, readOnly = true)
    public GetMyGifticonOrdersResult getMyGifticonOrders(GetMyGifticonOrdersCommand command) {
        GifticonOrderStatusFilter statusFilter = GifticonOrderStatusFilter.from(command.statusFilter());
        GifticonOrderSortType sortType = GifticonOrderSortType.from(command.sortType());
        List<GifticonOrderStatus> statuses = statusFilter.toStatuses();
        int fetchSize = command.pageSize() + 1;

        List<GifticonOrder> fetched = findGifticonOrderPort.findByUserIdAndStatuses(
                command.userId(), statuses, sortType, command.cursor(), fetchSize
        );

        boolean hasNext = fetched.size() > command.pageSize();
        List<GifticonOrder> orders = hasNext ? fetched.subList(0, command.pageSize()) : fetched;
        Long nextCursor = resolveNextCursor(sortType, hasNext, orders, command);

        long availableCount = findGifticonOrderPort.countByUserIdAndStatuses(
                command.userId(), GifticonOrderStatusFilter.AVAILABLE.toStatuses()
        );
        long completedOrExpiredCount = findGifticonOrderPort.countByUserIdAndStatuses(
                command.userId(), GifticonOrderStatusFilter.COMPLETED_OR_EXPIRED.toStatuses()
        );

        LocalDate now = LocalDate.now(clock);
        List<MyGifticonOrderItem> items = orders.stream()
                .map(order -> toItem(order, now))
                .toList();

        return new GetMyGifticonOrdersResult(
                availableCount, completedOrExpiredCount, hasNext, nextCursor, items
        );
    }

    private Long resolveNextCursor(GifticonOrderSortType sortType, boolean hasNext,
                                   List<GifticonOrder> orders, GetMyGifticonOrdersCommand command) {
        if (!hasNext) {
            return null;
        }
        if (sortType.isPurchaseLatest()) {
            return orders.get(orders.size() - 1).getId();
        }
        long currentOffset = (command.cursor() <= 0) ? 0 : command.cursor();
        return currentOffset + command.pageSize();
    }

    private MyGifticonOrderItem toItem(GifticonOrder order, LocalDate now) {
        return new MyGifticonOrderItem(
                order.getId(),
                order.getGoodsName(),
                order.getBrandName(),
                order.getProductImageUrl(),
                order.getCashPrice(),
                order.formatExpiryDate(),
                order.calculateDaysRemaining(now),
                order.resolveStatusLabel(),
                order.getOrderStatus().name(),
                order.getCreatedAt()
        );
    }
}
