package com.personal.marketnote.product.adapter.in.configuration;

import com.personal.marketnote.common.utility.FormatValidator;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class ProductSwaggerConfig {
    @Value("${server.origin}")
    private String serverOrigin;

    private static final List<String> TAGS_ORDER = List.of(
            "카테고리 API",
            "상품 API",
            "상품 가격 정책 API",
            "상품 옵션 API",
            "장바구니 API",
            "상품 주문 API",
            "배송비 정책 API",
            "DLT API",
            "서버 정보 API"
    );

    private static final Map<String, Integer> ORDER_MAP = new ConcurrentHashMap<>();

    static {
        for (int i = 0; i < TAGS_ORDER.size(); i++) {
            ORDER_MAP.put(TAGS_ORDER.get(i), i);
        }
    }

    @Bean
    public OpenApiCustomizer tagOnlySorter() {
        return (OpenAPI openApi) -> {
            if (FormatValidator.hasNoValue(openApi.getTags())) {
                return;
            }

            Map<String, Integer> originalIndex = new ConcurrentHashMap<>();
            for (int i = 0; i < openApi.getTags().size(); i++) {
                originalIndex.put(openApi.getTags().get(i).getName(), i);
            }

            Comparator<Tag> comparator = (t1, t2) -> {
                Integer p1 = ORDER_MAP.get(t1.getName());
                Integer p2 = ORDER_MAP.get(t2.getName());

                if (FormatValidator.hasNoValue(p1) && FormatValidator.hasNoValue(p2)) {
                    return Integer.compare(
                            originalIndex.getOrDefault(t1.getName(), Integer.MAX_VALUE),
                            originalIndex.getOrDefault(t2.getName(), Integer.MAX_VALUE)
                    );
                }

                if (FormatValidator.hasNoValue(p1)) {
                    return 1;
                }

                if (FormatValidator.hasNoValue(p2)) {
                    return -1;
                }

                return Integer.compare(p1, p2);
            };

            openApi.setTags(
                    openApi.getTags().stream()
                            .sorted(comparator)
                            .toList()
            );
        };
    }

    @Bean
    public OpenAPI apiV1() {
        OpenAPI openApi = new OpenAPI();

        io.swagger.v3.oas.models.security.SecurityScheme securityScheme = new io.swagger.v3.oas.models.security.SecurityScheme()
                .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                .name(HttpHeaders.AUTHORIZATION);

        openApi.info(new Info()
                        .title("마켓노트 상품 API 서버")
                        .description("""
                                마켓노트 서비스 상품 API 서버입니다.
                                
                                **개발: 성효빈**
                                
                                ### 서비스 목록
                                - [회원 서비스](https://users.marketnote.store/swagger-ui/index.html)
                                
                                - [상품 서비스](https://products.marketnote.store/swagger-ui/index.html)
                                
                                - [커머스 서비스](https://commerce.marketnote.store/swagger-ui/index.html)
                                
                                - [풀필먼트 서비스](https://fulfillment.marketnote.store/swagger-ui/index.html)
                                
                                - [커뮤니티 서비스](https://community.marketnote.store/swagger-ui/index.html)
                                
                                - [리워드 서비스](https://rewards.marketnote.store/swagger-ui/index.html)
                                
                                - [파일 서비스](https://files.marketnote.store/swagger-ui/index.html)
                                """)
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("bearer", securityScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearer"))
                .servers(List.of(
                        new Server().url(serverOrigin)
                ));

        return openApi;
    }

    private void addAuthorizationInfo(Operation operation) {
        if (FormatValidator.hasValue(operation)) {
            operation.getResponses().addApiResponse(
                            "401",
                            new ApiResponse().description("Bearer Token is invalid or no bearer token")
                                    .content(
                                            new Content().addMediaType("*/*",
                                                    new MediaType().addExamples("Not authenticated",
                                                            new Example().value("""
                                                                    {
                                                                        "statusCode": 401,
                                                                        "timestamp": "2025-12-26T19:56:58.7278032",
                                                                        "errorCode": "AUTH001",
                                                                        "errorName": "AUTHENTICATION_FAILED",
                                                                        "message": "Authentication Failed"
                                                                    }
                                                                    """))
                                            )
                                    )
                    )
                    .addApiResponse(
                            "403",
                            new ApiResponse().description("You are authenticated but not allowed authorization")
                                    .content(
                                            new Content().addMediaType("*/*",
                                                    new MediaType().addExamples("Not authorized",
                                                            new Example().value("""
                                                                    {
                                                                        "statusCode": 403,
                                                                        "timestamp": "2025-12-26T19:56:58.7278032",
                                                                        "errorCode": "AUTH002",
                                                                        "errorName": "AUTHORIZATION_FAILED",
                                                                        "message": "Authorization Failed"
                                                                    }
                                                                    """))
                                            )
                                    )
                    );
        }
    }
}
