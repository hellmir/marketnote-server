package com.personal.marketnote.notification.adapter.out.persistence.aligo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.configuration.AuditConfig;
import com.personal.marketnote.common.configuration.ObjectMapperConfig;
import com.personal.marketnote.notification.adapter.out.persistence.aligo.repository.AligoBrandTalkTemplateJpaRepository;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplate;
import com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplateCreateState;
import com.personal.marketnote.notification.domain.aligo.AligoTemplateButtonLinkType;
import com.personal.marketnote.notification.domain.aligo.BrandTalkButton;
import com.personal.marketnote.notification.domain.aligo.BrandTalkButtonCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselCommerce;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselCommerceCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselItem;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselItemCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCarouselTail;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCommerceInfo;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCommerceInfoCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCoupon;
import com.personal.marketnote.notification.domain.aligo.BrandTalkCouponCreateState;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateStatus;
import com.personal.marketnote.notification.domain.aligo.BrandTalkTemplateType;
import com.personal.marketnote.notification.domain.aligo.BrandTalkWideItem;
import com.personal.marketnote.notification.domain.aligo.BrandTalkWideItemCreateState;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({AuditConfig.class, ObjectMapperConfig.class, AligoBrandTalkTemplatePersistenceAdapter.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("AligoBrandTalkTemplatePersistenceAdapter 테스트")
class AligoBrandTalkTemplatePersistenceAdapterTest {

    @Autowired
    private AligoBrandTalkTemplatePersistenceAdapter adapter;

    @Autowired
    private AligoBrandTalkTemplateJpaRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("복합 JSON 필드(버튼/쿠폰/와이드아이템/캐러셀/커머스)를 직렬화하고 다시 역직렬화하여 동일한 값을 복원한다")
    void shouldRoundTripAllCompositeJsonFields() {
        // given
        BrandTalkButton button = BrandTalkButton.from(
                BrandTalkButtonCreateState.builder()
                        .name("자세히 보기")
                        .linkType(AligoTemplateButtonLinkType.WL)
                        .linkM("https://m.example.com")
                        .linkP("https://example.com")
                        .build()
        );
        BrandTalkCoupon coupon = BrandTalkCoupon.from(
                BrandTalkCouponCreateState.builder()
                        .title("10% 쿠폰")
                        .description("신규 가입자 대상")
                        .linkM("https://m.example.com/coupon")
                        .linkP("https://example.com/coupon")
                        .build()
        );
        BrandTalkWideItem mainItem = BrandTalkWideItem.from(
                BrandTalkWideItemCreateState.builder()
                        .title("메인 와이드")
                        .imageUrl("https://cdn.example.com/main.png")
                        .linkM("https://m.example.com/main")
                        .linkP("https://example.com/main")
                        .build()
        );
        BrandTalkWideItem sub1 = BrandTalkWideItem.from(
                BrandTalkWideItemCreateState.builder()
                        .title("서브1")
                        .imageUrl("https://cdn.example.com/sub1.png")
                        .linkM("https://m.example.com/sub1")
                        .linkP("https://example.com/sub1")
                        .build()
        );
        BrandTalkWideItem sub2 = BrandTalkWideItem.from(
                BrandTalkWideItemCreateState.builder()
                        .title("서브2")
                        .imageUrl("https://cdn.example.com/sub2.png")
                        .linkM("https://m.example.com/sub2")
                        .linkP("https://example.com/sub2")
                        .build()
        );
        BrandTalkCarouselItem carouselItem = BrandTalkCarouselItem.from(
                BrandTalkCarouselItemCreateState.builder()
                        .header("캐러셀 헤더")
                        .content("캐러셀 본문")
                        .imageUrl("https://cdn.example.com/carousel.png")
                        .buttons(List.of(button))
                        .coupon(coupon)
                        .build()
        );
        BrandTalkCarouselCommerce commerce = BrandTalkCarouselCommerce.from(
                BrandTalkCarouselCommerceCreateState.builder()
                        .title("상품A")
                        .regularPrice("10000")
                        .discountPrice("8000")
                        .discountRate("20")
                        .build()
        );
        BrandTalkCarouselTail carouselTail = BrandTalkCarouselTail.of("https://m.example.com/tail");
        BrandTalkCommerceInfo commerceInfo = BrandTalkCommerceInfo.from(
                BrandTalkCommerceInfoCreateState.builder()
                        .title("메인 상품")
                        .regularPrice("20000")
                        .discountPrice("15000")
                        .build()
        );
        AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(
                AligoBrandTalkTemplateCreateState.builder()
                        .senderKey("sender-key-1")
                        .templateCode("TPL-ROUND-TRIP-001")
                        .templateName("라운드트립 템플릿")
                        .templateType(BrandTalkTemplateType.TEXT)
                        .templateContent("테스트 본문")
                        .adult(false)
                        .buttons(List.of(button))
                        .coupon(coupon)
                        .templateHeader("헤더")
                        .mainWideItem(mainItem)
                        .subWideItems(List.of(sub1, sub2))
                        .carouselHeader("캐러셀")
                        .carouselItems(List.of(carouselItem))
                        .carouselCommerces(List.of(commerce))
                        .carouselTail(carouselTail)
                        .videoUrl("https://cdn.example.com/video.mp4")
                        .videoThumbnailUrl("https://cdn.example.com/video-thumb.png")
                        .commerceInfo(commerceInfo)
                        .templateStatus(BrandTalkTemplateStatus.NORMAL)
                        .build()
        );

        // when
        adapter.save(template);
        entityManager.flush();
        entityManager.clear();

        Optional<AligoBrandTalkTemplate> reloaded = adapter.findByTemplateCode("TPL-ROUND-TRIP-001");

        // then
        assertThat(reloaded).isPresent();
        AligoBrandTalkTemplate result = reloaded.get();
        assertThat(result.getButtons()).hasSize(1);
        assertThat(result.getButtons().get(0).getName()).isEqualTo("자세히 보기");
        assertThat(result.getButtons().get(0).getLinkType()).isEqualTo(AligoTemplateButtonLinkType.WL);
        assertThat(result.getCoupon()).isNotNull();
        assertThat(result.getCoupon().getTitle()).isEqualTo("10% 쿠폰");
        assertThat(result.getMainWideItem()).isNotNull();
        assertThat(result.getMainWideItem().getTitle()).isEqualTo("메인 와이드");
        assertThat(result.getSubWideItems()).hasSize(2);
        assertThat(result.getSubWideItems())
                .extracting(BrandTalkWideItem::getTitle)
                .containsExactly("서브1", "서브2");
        assertThat(result.getCarouselItems()).hasSize(1);
        assertThat(result.getCarouselItems().get(0).getHeader()).isEqualTo("캐러셀 헤더");
        assertThat(result.getCarouselItems().get(0).getButtons()).hasSize(1);
        assertThat(result.getCarouselItems().get(0).getCoupon()).isNotNull();
        assertThat(result.getCarouselCommerces()).hasSize(1);
        assertThat(result.getCarouselCommerces().get(0).getTitle()).isEqualTo("상품A");
        assertThat(result.getCarouselTail()).isNotNull();
        assertThat(result.getCarouselTail().getLinkM()).isEqualTo("https://m.example.com/tail");
        assertThat(result.getCommerceInfo()).isNotNull();
        assertThat(result.getCommerceInfo().getTitle()).isEqualTo("메인 상품");
    }

    @Test
    @DisplayName("모든 복합 필드가 NULL 이면 JSON 컬럼에도 NULL 로 저장된다")
    void shouldStoreNullWhenAllCompositeFieldsAreNull() {
        // given
        AligoBrandTalkTemplate template = AligoBrandTalkTemplate.from(
                AligoBrandTalkTemplateCreateState.builder()
                        .senderKey("sender-key-null")
                        .templateCode("TPL-NULL-001")
                        .templateName("NULL 템플릿")
                        .templateType(BrandTalkTemplateType.TEXT)
                        .templateContent("본문")
                        .adult(false)
                        .templateStatus(BrandTalkTemplateStatus.NORMAL)
                        .build()
        );

        // when
        adapter.save(template);
        entityManager.flush();
        entityManager.clear();

        // then
        var entity = repository.findByTemplateCode("TPL-NULL-001").orElseThrow();
        assertThat(entity.getButtonsJson()).isNull();
        assertThat(entity.getCouponJson()).isNull();
        assertThat(entity.getMainWideItemJson()).isNull();
        assertThat(entity.getSubWideItemsJson()).isNull();
        assertThat(entity.getCarouselItemsJson()).isNull();
        assertThat(entity.getCarouselCommercesJson()).isNull();
        assertThat(entity.getCarouselTailJson()).isNull();
        assertThat(entity.getCommerceInfoJson()).isNull();
    }

    @Test
    @DisplayName("update 호출 시 templateStatus 만 변경되고 다른 필드는 유지된다")
    void shouldUpdateOnlyTemplateStatusOnPartialUpdate() {
        // given
        AligoBrandTalkTemplate saved = adapter.save(buildSimpleTemplate("TPL-UPDATE-001", "sender-key-update"));
        entityManager.flush();
        entityManager.clear();

        AligoBrandTalkTemplate request = AligoBrandTalkTemplate.from(
                com.personal.marketnote.notification.domain.aligo.AligoBrandTalkTemplateSnapshotState.builder()
                        .id(saved.getId())
                        .senderKey("다른-sender-key")
                        .templateCode("TPL-UPDATE-001")
                        .templateName("다른-이름")
                        .templateType(BrandTalkTemplateType.TEXT)
                        .templateContent("다른-본문")
                        .adult(true)
                        .templateStatus(BrandTalkTemplateStatus.STOPPED)
                        .status(com.personal.marketnote.common.domain.EntityStatus.ACTIVE)
                        .build()
        );

        // when
        adapter.update(request);
        entityManager.flush();
        entityManager.clear();

        // then
        var entity = repository.findByTemplateCode("TPL-UPDATE-001").orElseThrow();
        assertThat(entity.getTemplateStatus()).isEqualTo(BrandTalkTemplateStatus.STOPPED);
        assertThat(entity.getSenderKey()).isEqualTo("sender-key-update");
        assertThat(entity.getTemplateName()).isEqualTo("초기 템플릿");
        assertThat(entity.getTemplateContent()).isEqualTo("초기 본문");
        assertThat(entity.isAdult()).isFalse();
    }

    @Test
    @DisplayName("findAllBySenderKey 는 동일 senderKey 의 모든 템플릿을 반환하고 다른 senderKey 는 제외한다")
    void shouldReturnOnlyTemplatesForGivenSenderKey() {
        // given
        adapter.save(buildSimpleTemplate("TPL-LIST-001", "sender-key-list"));
        adapter.save(buildSimpleTemplate("TPL-LIST-002", "sender-key-list"));
        adapter.save(buildSimpleTemplate("TPL-LIST-003", "sender-key-list"));
        adapter.save(buildSimpleTemplate("TPL-OTHER-001", "other-sender-key"));
        entityManager.flush();
        entityManager.clear();

        // when
        List<AligoBrandTalkTemplate> result = adapter.findAllBySenderKey("sender-key-list");

        // then
        assertThat(result).hasSize(3);
        assertThat(result)
                .extracting(AligoBrandTalkTemplate::getTemplateCode)
                .containsExactlyInAnyOrder("TPL-LIST-001", "TPL-LIST-002", "TPL-LIST-003");
    }

    private AligoBrandTalkTemplate buildSimpleTemplate(String templateCode, String senderKey) {
        return AligoBrandTalkTemplate.from(
                AligoBrandTalkTemplateCreateState.builder()
                        .senderKey(senderKey)
                        .templateCode(templateCode)
                        .templateName("초기 템플릿")
                        .templateType(BrandTalkTemplateType.TEXT)
                        .templateContent("초기 본문")
                        .adult(false)
                        .templateStatus(BrandTalkTemplateStatus.NORMAL)
                        .build()
        );
    }
}
