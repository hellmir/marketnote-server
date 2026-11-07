package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselCommerce;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselItem;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselTail;
import com.personal.marketnote.notification.port.in.command.BrandTalkButtonCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCarouselCommerceCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCarouselItemCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCarouselTailCommand;
import com.personal.marketnote.notification.port.in.command.BrandTalkCouponCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BrandTalkCarouselCommandToStateMapperTest {

    @Test
    @DisplayName("캐러셀 아이템 Command는 버튼 매핑을 포함하여 도메인으로 매핑된다")
    void mapsCarouselItemCommand() {
        BrandTalkCarouselItemCommand command = new BrandTalkCarouselItemCommand(
                "헤더", "컨텐츠",
                List.of(new BrandTalkButtonCommand("버튼A", "AC", null, null, null, null)),
                null
        );

        BrandTalkCarouselItem item = BrandTalkCarouselCommandToStateMapper.mapCarouselItem(command);

        assertThat(item).isNotNull();
        assertThat(item.getHeader()).isEqualTo("헤더");
        assertThat(item.getContent()).isEqualTo("컨텐츠");
        assertThat(item.getButtons()).hasSize(1);
        assertThat(item.getButtons().get(0).getName()).isEqualTo("버튼A");
    }

    @Test
    @DisplayName("캐러셀 아이템 Command 리스트는 도메인 리스트로 매핑된다")
    void mapsCarouselItemCommandList() {
        List<BrandTalkCarouselItemCommand> commands = List.of(
                new BrandTalkCarouselItemCommand(
                        "헤더1", "컨텐츠1",
                        List.of(new BrandTalkButtonCommand("버튼A", "AC", null, null, null, null)),
                        null
                ),
                new BrandTalkCarouselItemCommand(
                        "헤더2", "컨텐츠2",
                        List.of(new BrandTalkButtonCommand("버튼B", "AC", null, null, null, null)),
                        null
                )
        );

        List<BrandTalkCarouselItem> items = BrandTalkCarouselCommandToStateMapper.mapCarouselItems(commands);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getHeader()).isEqualTo("헤더1");
        assertThat(items.get(1).getHeader()).isEqualTo("헤더2");
    }

    @Test
    @DisplayName("캐러셀 아이템 Command 리스트가 null이면 빈 리스트를 반환한다")
    void returnsEmptyListWhenItemCommandsNull() {
        List<BrandTalkCarouselItem> items = BrandTalkCarouselCommandToStateMapper.mapCarouselItems(null);

        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("mapCarouselItems는 빈 리스트 입력 시 빈 리스트를 반환한다")
    void returnsEmptyListWhenItemCommandsEmpty() {
        List<BrandTalkCarouselItem> items = BrandTalkCarouselCommandToStateMapper.mapCarouselItems(List.of());

        assertThat(items).isEmpty();
    }

    @Test
    @DisplayName("mapCarouselItem은 coupon이 설정되면 도메인 쿠폰을 매핑한다")
    void mapsCarouselItemWithCoupon() {
        BrandTalkCouponCommand couponCommand = new BrandTalkCouponCommand(
                "쿠폰 타이틀", "쿠폰 설명",
                "https://m.example.com", "https://pc.example.com", null, null
        );
        BrandTalkCarouselItemCommand command = new BrandTalkCarouselItemCommand(
                "헤더", "컨텐츠",
                List.of(new BrandTalkButtonCommand("버튼A", "AC", null, null, null, null)),
                couponCommand
        );

        BrandTalkCarouselItem item = BrandTalkCarouselCommandToStateMapper.mapCarouselItem(command);

        assertThat(item.getCoupon()).isNotNull();
        assertThat(item.getCoupon().getTitle()).isEqualTo("쿠폰 타이틀");
        assertThat(item.getCoupon().getDescription()).isEqualTo("쿠폰 설명");
        assertThat(item.getCoupon().getLinkM()).isEqualTo("https://m.example.com");
        assertThat(item.getCoupon().getLinkP()).isEqualTo("https://pc.example.com");
    }

    @Test
    @DisplayName("캐러셀 커머스 Command는 도메인으로 매핑된다")
    void mapsCarouselCommerceCommand() {
        BrandTalkCarouselCommerceCommand command = new BrandTalkCarouselCommerceCommand(
                "상품명", "10000", "8000", "20", null
        );

        BrandTalkCarouselCommerce commerce = BrandTalkCarouselCommandToStateMapper.mapCarouselCommerce(command);

        assertThat(commerce).isNotNull();
        assertThat(commerce.getTitle()).isEqualTo("상품명");
        assertThat(commerce.getRegularPrice()).isEqualTo("10000");
        assertThat(commerce.getDiscountPrice()).isEqualTo("8000");
        assertThat(commerce.getDiscountRate()).isEqualTo("20");
    }

    @Test
    @DisplayName("캐러셀 커머스 Command 리스트는 도메인 리스트로 매핑된다")
    void mapsCarouselCommerceCommandList() {
        List<BrandTalkCarouselCommerceCommand> commands = List.of(
                new BrandTalkCarouselCommerceCommand("A", "1000", null, null, null),
                new BrandTalkCarouselCommerceCommand("B", "2000", null, null, null)
        );

        List<BrandTalkCarouselCommerce> list = BrandTalkCarouselCommandToStateMapper.mapCarouselCommerces(commands);

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getTitle()).isEqualTo("A");
        assertThat(list.get(1).getTitle()).isEqualTo("B");
    }

    @Test
    @DisplayName("캐러셀 커머스 Command 리스트가 null이면 빈 리스트를 반환한다")
    void returnsEmptyListWhenCommerceCommandsNull() {
        List<BrandTalkCarouselCommerce> list = BrandTalkCarouselCommandToStateMapper.mapCarouselCommerces(null);

        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("mapCarouselCommerces는 빈 리스트 입력 시 빈 리스트를 반환한다")
    void returnsEmptyListWhenCommerceCommandsEmpty() {
        List<BrandTalkCarouselCommerce> list = BrandTalkCarouselCommandToStateMapper.mapCarouselCommerces(List.of());

        assertThat(list).isEmpty();
    }

    @Test
    @DisplayName("캐러셀 테일 Command는 도메인으로 매핑된다")
    void mapsCarouselTailCommand() {
        BrandTalkCarouselTailCommand command = new BrandTalkCarouselTailCommand("https://m.example.com");

        BrandTalkCarouselTail tail = BrandTalkCarouselCommandToStateMapper.mapCarouselTail(command);

        assertThat(tail).isNotNull();
        assertThat(tail.getLinkM()).isEqualTo("https://m.example.com");
    }

    @Test
    @DisplayName("캐러셀 테일 Command가 null이면 null을 반환한다")
    void returnsNullWhenTailCommandNull() {
        BrandTalkCarouselTail tail = BrandTalkCarouselCommandToStateMapper.mapCarouselTail(null);

        assertThat(tail).isNull();
    }
}
