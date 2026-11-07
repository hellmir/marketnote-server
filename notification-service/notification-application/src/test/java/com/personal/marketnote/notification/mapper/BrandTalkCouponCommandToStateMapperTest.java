package com.personal.marketnote.notification.mapper;

import com.personal.marketnote.notification.domain.aligo.BrandTalkCoupon;
import com.personal.marketnote.notification.port.in.command.BrandTalkCouponCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BrandTalkCouponCommandToStateMapperTest {

    @Test
    @DisplayName("Coupon Command가 null이면 null을 반환한다")
    void returnsNullWhenCouponCommandNull() {
        BrandTalkCoupon coupon = BrandTalkCouponCommandToStateMapper.mapCoupon(null);

        assertThat(coupon).isNull();
    }

    @Test
    @DisplayName("Coupon Command가 있으면 BrandTalkCoupon 도메인으로 매핑된다")
    void mapsCouponCommand() {
        BrandTalkCouponCommand command = new BrandTalkCouponCommand(
                "10% 할인 쿠폰",
                "회원 전용 혜택",
                "https://m.example.com",
                "https://pc.example.com",
                "iosapp://coupon",
                "androidapp://coupon"
        );

        BrandTalkCoupon coupon = BrandTalkCouponCommandToStateMapper.mapCoupon(command);

        assertThat(coupon).isNotNull();
        assertThat(coupon.getTitle()).isEqualTo("10% 할인 쿠폰");
        assertThat(coupon.getDescription()).isEqualTo("회원 전용 혜택");
        assertThat(coupon.getLinkM()).isEqualTo("https://m.example.com");
        assertThat(coupon.getLinkP()).isEqualTo("https://pc.example.com");
        assertThat(coupon.getLinkI()).isEqualTo("iosapp://coupon");
        assertThat(coupon.getLinkA()).isEqualTo("androidapp://coupon");
    }
}
