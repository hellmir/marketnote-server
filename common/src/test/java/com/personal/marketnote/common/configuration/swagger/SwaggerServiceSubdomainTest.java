package com.personal.marketnote.common.configuration.swagger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SwaggerServiceSubdomainTest {

    @Test
    @DisplayName("9개 서브도메인이 정의되어 있다")
    void definesAllNineSubdomains() {
        assertThat(SwaggerServiceSubdomain.values()).hasSize(9);
    }

    @Test
    @DisplayName("회원 서비스 마크다운 링크를 baseHost 기반으로 조립한다")
    void buildsUsersMarkdownLink() {
        String link = SwaggerServiceSubdomain.USERS.markdownLink("https", "marketnote.store");
        assertThat(link).isEqualTo("- [회원 서비스](https://users.marketnote.store/swagger-ui/index.html)");
    }

    @Test
    @DisplayName("상품 서비스 마크다운 링크를 baseHost 기반으로 조립한다")
    void buildsProductsMarkdownLink() {
        String link = SwaggerServiceSubdomain.PRODUCTS.markdownLink("https", "marketnote.store");
        assertThat(link).isEqualTo("- [상품 서비스](https://products.marketnote.store/swagger-ui/index.html)");
    }

    @Test
    @DisplayName("커머스 서비스 마크다운 링크를 baseHost 기반으로 조립한다")
    void buildsCommerceMarkdownLink() {
        String link = SwaggerServiceSubdomain.COMMERCE.markdownLink("https", "marketnote.store");
        assertThat(link).isEqualTo("- [커머스 서비스](https://commerce.marketnote.store/swagger-ui/index.html)");
    }

    @Test
    @DisplayName("풀필먼트 서비스 마크다운 링크를 baseHost 기반으로 조립한다")
    void buildsFulfillmentMarkdownLink() {
        String link = SwaggerServiceSubdomain.FULFILLMENT.markdownLink("https", "marketnote.store");
        assertThat(link).isEqualTo("- [풀필먼트 서비스](https://fulfillment.marketnote.store/swagger-ui/index.html)");
    }

    @Test
    @DisplayName("커뮤니티 서비스 마크다운 링크를 baseHost 기반으로 조립한다")
    void buildsCommunityMarkdownLink() {
        String link = SwaggerServiceSubdomain.COMMUNITY.markdownLink("https", "marketnote.store");
        assertThat(link).isEqualTo("- [커뮤니티 서비스](https://community.marketnote.store/swagger-ui/index.html)");
    }

    @Test
    @DisplayName("리워드 서비스 마크다운 링크를 baseHost 기반으로 조립한다")
    void buildsRewardsMarkdownLink() {
        String link = SwaggerServiceSubdomain.REWARDS.markdownLink("https", "marketnote.store");
        assertThat(link).isEqualTo("- [리워드 서비스](https://rewards.marketnote.store/swagger-ui/index.html)");
    }

    @Test
    @DisplayName("알림 서비스 마크다운 링크를 baseHost 기반으로 조립한다")
    void buildsNotificationsMarkdownLink() {
        String link = SwaggerServiceSubdomain.NOTIFICATIONS.markdownLink("https", "marketnote.store");
        assertThat(link).isEqualTo("- [알림 서비스](https://notifications.marketnote.store/swagger-ui/index.html)");
    }

    @Test
    @DisplayName("파일 서비스 마크다운 링크를 baseHost 기반으로 조립한다")
    void buildsFilesMarkdownLink() {
        String link = SwaggerServiceSubdomain.FILES.markdownLink("https", "marketnote.store");
        assertThat(link).isEqualTo("- [파일 서비스](https://files.marketnote.store/swagger-ui/index.html)");
    }

    @Test
    @DisplayName("모니터링 대시보드 링크는 인증 필요 안내가 붙는다")
    void buildsMonitoringMarkdownLinkWithAuthHint() {
        String link = SwaggerServiceSubdomain.MONITORING.markdownLink("https", "marketnote.store");
        assertThat(link).isEqualTo(
                "- [모니터링 대시보드](https://monitoring.marketnote.store/d/spring_boot_21/spring-boot-3-x-statistics) (인증 필요)"
        );
    }

    @Test
    @DisplayName("scheme이 http이면 그대로 사용한다")
    void usesGivenScheme() {
        String link = SwaggerServiceSubdomain.COMMERCE.markdownLink("http", "localhost:3000");
        assertThat(link).isEqualTo("- [커머스 서비스](http://commerce.localhost:3000/swagger-ui/index.html)");
    }
}
