package com.personal.marketnote.common.configuration.swagger;

public enum SwaggerServiceSubdomain {
    USERS("users", "회원 서비스", "/swagger-ui/index.html", ""),
    PRODUCTS("products", "상품 서비스", "/swagger-ui/index.html", ""),
    COMMERCE("commerce", "커머스 서비스", "/swagger-ui/index.html", ""),
    FULFILLMENT("fulfillment", "풀필먼트 서비스", "/swagger-ui/index.html", ""),
    COMMUNITY("community", "커뮤니티 서비스", "/swagger-ui/index.html", ""),
    REWARDS("rewards", "리워드 서비스", "/swagger-ui/index.html", ""),
    NOTIFICATIONS("notifications", "알림 서비스", "/swagger-ui/index.html", ""),
    FILES("files", "파일 서비스", "/swagger-ui/index.html", ""),
    MONITORING(
            "monitoring",
            "모니터링 대시보드",
            "/d/spring_boot_21/spring-boot-3-x-statistics",
            " (인증 필요)"
    );

    private final String subdomain;
    private final String label;
    private final String path;
    private final String suffix;

    SwaggerServiceSubdomain(String subdomain, String label, String path, String suffix) {
        this.subdomain = subdomain;
        this.label = label;
        this.path = path;
        this.suffix = suffix;
    }

    public String markdownLink(String scheme, String baseHost) {
        return "- [" + label + "](" + scheme + "://" + subdomain + "." + baseHost + path + ")" + suffix;
    }
}
