package com.personal.marketnote.notification.adapter.out.persistence.template;

import com.personal.marketnote.common.configuration.AuditConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(AuditConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:seed_migration_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/migration",
        "spring.jpa.hibernate.ddl-auto=none"
})
class NotificationTemplateSeedMigrationTest {

    private static final int EXPECTED_TOTAL_TEMPLATES = 21;
    private static final int EXPECTED_MANDATORY = 13;
    private static final int EXPECTED_INFORMATIONAL = 6;
    private static final int EXPECTED_PROMOTIONAL = 2;

    private static final Set<String> EXPECTED_TEMPLATE_CODES = Set.of(
            "ORDER_PAYMENT_COMPLETED",
            "SHIPPING_STARTED",
            "SHIPPING_COMPLETED",
            "SHIPPING_DELAYED",
            "ORDER_CANCELLED",
            "ORDER_CANCEL_FAILED",
            "RETURN_REQUESTED",
            "RETURN_COMPLETED",
            "RETURN_REJECTED",
            "ONE_ON_ONE_INQUIRY_REPLY",
            "PRODUCT_INQUIRY_REPLY",
            "NOTICE_REGISTERED",
            "CONSENT_REMINDER",
            "PURCHASE_CONFIRMATION_REQUEST",
            "PURCHASE_CONFIRMATION_COMPLETED",
            "PURCHASE_CONFIRMATION_COMPLETED_AUTO",
            "PURCHASE_CONFIRMATION_COMPLETED_MANUAL",
            "REVIEW_REPLY",
            "POINT_ACCRUAL",
            "EVENT_REGISTERED",
            "REVIEW_WRITE_REQUEST"
    );

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("V2 마이그레이션 적용 후 Consumer 실제 사용 template_code 21종 전체가 시드된다")
    void shouldSeedAllTemplates() {
        Number count = countAllTemplates();
        assertThat(count.longValue()).isEqualTo((long) EXPECTED_TOTAL_TEMPLATES);
    }

    @Test
    @DisplayName("모든 시드 row의 status는 ACTIVE이다")
    void shouldAllSeededRowsHaveActiveStatus() {
        Number activeCount = (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM notification_template WHERE status = 'ACTIVE'")
                .getSingleResult();
        assertThat(activeCount.longValue()).isEqualTo((long) EXPECTED_TOTAL_TEMPLATES);
    }

    @Test
    @DisplayName("MANDATORY 카테고리 template이 13개 시드된다")
    void shouldSeedMandatoryTemplates() {
        Number count = countByCategory("MANDATORY");
        assertThat(count.longValue()).isEqualTo((long) EXPECTED_MANDATORY);
    }

    @Test
    @DisplayName("INFORMATIONAL 카테고리 template이 6개 시드된다")
    void shouldSeedSixInformationalTemplates() {
        Number count = countByCategory("INFORMATIONAL");
        assertThat(count.longValue()).isEqualTo((long) EXPECTED_INFORMATIONAL);
    }

    @Test
    @DisplayName("PROMOTIONAL 카테고리 template이 2개 시드된다")
    void shouldSeedTwoPromotionalTemplates() {
        Number count = countByCategory("PROMOTIONAL");
        assertThat(count.longValue()).isEqualTo((long) EXPECTED_PROMOTIONAL);
    }

    @Test
    @DisplayName("각 template_code는 중복 없이 정확히 1개씩 존재한다")
    void shouldHaveUniqueTemplateCodes() {
        @SuppressWarnings("unchecked")
        List<String> codes = entityManager
                .createNativeQuery("SELECT template_code FROM notification_template")
                .getResultList();
        assertThat(codes).hasSize(EXPECTED_TOTAL_TEMPLATES);
        assertThat(codes).doesNotHaveDuplicates();
    }

    @Test
    @DisplayName("시드된 template_code 집합은 Consumer가 사용하는 21종과 정확히 일치한다")
    void shouldMatchExpectedTemplateCodes() {
        @SuppressWarnings("unchecked")
        List<String> codes = entityManager
                .createNativeQuery("SELECT template_code FROM notification_template")
                .getResultList();
        assertThat(codes).containsExactlyInAnyOrderElementsOf(EXPECTED_TEMPLATE_CODES);
    }

    @Test
    @DisplayName("title, body_template, notification_type, notification_category 컬럼은 NOT NULL이다")
    void shouldAllRequiredFieldsBeNotNull() {
        Number nullCount = (Number) entityManager
                .createNativeQuery("""
                        SELECT COUNT(*) FROM notification_template
                        WHERE title IS NULL
                           OR body_template IS NULL
                           OR notification_type IS NULL
                           OR notification_category IS NULL
                        """)
                .getSingleResult();
        assertThat(nullCount.longValue()).isZero();
    }

    @Test
    @DisplayName("PURCHASE_CONFIRMATION_COMPLETED notification_type은 AUTO/MANUAL/기본 3개 template_code로 분할된다")
    void shouldHaveThreePurchaseConfirmationCompletedTemplates() {
        Number count = (Number) entityManager
                .createNativeQuery(
                        "SELECT COUNT(*) FROM notification_template WHERE notification_type = 'PURCHASE_CONFIRMATION_COMPLETED'"
                )
                .getSingleResult();
        assertThat(count.longValue()).isEqualTo(3L);
    }

    @Test
    @DisplayName("NOTICE_REGISTERED는 NotificationType.NOTICE에 매핑되고, EVENT_REGISTERED는 NotificationType.EVENT에 매핑된다")
    void shouldMapRegisteredTemplateCodesToCorrectNotificationType() {
        Number noticeCount = (Number) entityManager
                .createNativeQuery(
                        "SELECT COUNT(*) FROM notification_template WHERE template_code = 'NOTICE_REGISTERED' AND notification_type = 'NOTICE'"
                )
                .getSingleResult();
        Number eventCount = (Number) entityManager
                .createNativeQuery(
                        "SELECT COUNT(*) FROM notification_template WHERE template_code = 'EVENT_REGISTERED' AND notification_type = 'EVENT'"
                )
                .getSingleResult();
        assertThat(noticeCount.longValue()).isEqualTo(1L);
        assertThat(eventCount.longValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("동일 template_code로 재실행해도 중복 row가 생성되지 않는다 (멱등성)")
    void shouldBeIdempotentWhenReinsertingSameTemplateCode() {
        long beforeCount = countAllTemplates().longValue();

        int inserted = entityManager
                .createNativeQuery("""
                        INSERT INTO notification_template (status, created_at, template_code, notification_type, notification_category, title, body_template, url_template)
                        SELECT 'ACTIVE', NOW(), 'PRODUCT_INQUIRY_REPLY', 'PRODUCT_INQUIRY_REPLY', 'MANDATORY', '재시도 제목', '재시도 본문', '/retry'
                        WHERE NOT EXISTS (SELECT 1 FROM notification_template WHERE template_code = 'PRODUCT_INQUIRY_REPLY')
                        """)
                .executeUpdate();

        long afterCount = countAllTemplates().longValue();
        assertThat(inserted).isZero();
        assertThat(afterCount).isEqualTo(beforeCount);
    }

    @Test
    @DisplayName("PRODUCT_INQUIRY_REPLY 템플릿이 시드되어 silent drop 장애가 재발하지 않는다")
    void shouldSeedProductInquiryReplyTemplate() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager
                .createNativeQuery("""
                        SELECT title, body_template, url_template, notification_category
                          FROM notification_template
                         WHERE template_code = 'PRODUCT_INQUIRY_REPLY'
                        """)
                .getResultList();
        assertThat(rows).hasSize(1);
        Object[] row = rows.get(0);
        assertThat(row[0]).isEqualTo("[상품 문의]");
        assertThat((String) row[1]).contains("판매자가 답변을 남겼습니다");
        assertThat((String) row[2]).contains("board=PRODUCT_INQUERY");
        assertThat(row[3]).isEqualTo("MANDATORY");
    }

    private Number countAllTemplates() {
        return (Number) entityManager
                .createNativeQuery("SELECT COUNT(*) FROM notification_template")
                .getSingleResult();
    }

    private Number countByCategory(String category) {
        return (Number) entityManager
                .createNativeQuery(
                        "SELECT COUNT(*) FROM notification_template WHERE notification_category = :cat"
                )
                .setParameter("cat", category)
                .getSingleResult();
    }
}
