package com.personal.marketnote.reward.adapter.out.vendor.giftishow;

import com.personal.marketnote.reward.adapter.out.vendor.giftishow.exception.GiftishowOperationMappingException;
import com.personal.marketnote.reward.domain.vendorcommunication.RewardVendorCommunicationTargetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("GiftishowOperationTargetTypeResolver 테스트")
class GiftishowOperationTargetTypeResolverTest {

    private final GiftishowOperationTargetTypeResolver resolver = new GiftishowOperationTargetTypeResolver();

    @Test
    @DisplayName("product.list operation은 COUPON targetType으로 매핑된다")
    void shouldResolveProductListAsCoupon() {
        assertThat(resolver.resolve("product.list"))
            .isEqualTo(RewardVendorCommunicationTargetType.COUPON);
    }

    @Test
    @DisplayName("product.detail operation은 COUPON targetType으로 매핑된다")
    void shouldResolveProductDetailAsCoupon() {
        assertThat(resolver.resolve("product.detail"))
            .isEqualTo(RewardVendorCommunicationTargetType.COUPON);
    }

    @Test
    @DisplayName("brand.list operation은 COUPON targetType으로 매핑된다")
    void shouldResolveBrandListAsCoupon() {
        assertThat(resolver.resolve("brand.list"))
            .isEqualTo(RewardVendorCommunicationTargetType.COUPON);
    }

    @Test
    @DisplayName("brand.detail operation은 COUPON targetType으로 매핑된다")
    void shouldResolveBrandDetailAsCoupon() {
        assertThat(resolver.resolve("brand.detail"))
            .isEqualTo(RewardVendorCommunicationTargetType.COUPON);
    }

    @Test
    @DisplayName("coupon.send operation은 COUPON targetType으로 매핑된다")
    void shouldResolveCouponSendAsCoupon() {
        assertThat(resolver.resolve("coupon.send"))
            .isEqualTo(RewardVendorCommunicationTargetType.COUPON);
    }

    @Test
    @DisplayName("coupon.detail operation은 COUPON targetType으로 매핑된다")
    void shouldResolveCouponDetailAsCoupon() {
        assertThat(resolver.resolve("coupon.detail"))
            .isEqualTo(RewardVendorCommunicationTargetType.COUPON);
    }

    @Test
    @DisplayName("coupon.cancel operation은 COUPON targetType으로 매핑된다")
    void shouldResolveCouponCancelAsCoupon() {
        assertThat(resolver.resolve("coupon.cancel"))
            .isEqualTo(RewardVendorCommunicationTargetType.COUPON);
    }

    @Test
    @DisplayName("coupon.resend operation은 COUPON targetType으로 매핑된다")
    void shouldResolveCouponResendAsCoupon() {
        assertThat(resolver.resolve("coupon.resend"))
            .isEqualTo(RewardVendorCommunicationTargetType.COUPON);
    }

    @Test
    @DisplayName("coupon.sendFailCancel operation은 COUPON targetType으로 매핑된다")
    void shouldResolveCouponSendFailCancelAsCoupon() {
        assertThat(resolver.resolve("coupon.sendFailCancel"))
            .isEqualTo(RewardVendorCommunicationTargetType.COUPON);
    }

    @Test
    @DisplayName("bizmoney.balance operation은 BIZMONEY targetType으로 매핑된다")
    void shouldResolveBizmoneyBalanceAsBizmoney() {
        assertThat(resolver.resolve("bizmoney.balance"))
            .isEqualTo(RewardVendorCommunicationTargetType.BIZMONEY);
    }

    @Test
    @DisplayName("미등록 operation은 fail-open으로 기본 targetType(COUPON)을 반환한다")
    void shouldFailOpenForUnknownOperation() {
        assertThat(resolver.resolve("unknown.operation"))
            .isEqualTo(RewardVendorCommunicationTargetType.COUPON);
    }

    @Test
    @DisplayName("@PostConstruct 검증은 알려진 operation 10종이 모두 매핑되어 있으면 통과한다")
    void shouldPassValidationWhenAllOperationsAreMapped() {
        assertThatCode(resolver::validateMappingCompleteness)
            .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("GiftishowOperationMappingException은 RuntimeException이다")
    void shouldExtendRuntimeException() {
        assertThat(new GiftishowOperationMappingException("test"))
            .isInstanceOf(RuntimeException.class);
    }
}
