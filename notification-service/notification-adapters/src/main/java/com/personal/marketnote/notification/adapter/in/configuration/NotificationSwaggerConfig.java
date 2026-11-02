package com.personal.marketnote.notification.adapter.in.configuration;

import com.personal.marketnote.common.configuration.swagger.SwaggerEnvironment;
import com.personal.marketnote.common.configuration.swagger.SwaggerOrigin;
import com.personal.marketnote.common.configuration.swagger.SwaggerServiceSubdomain;
import com.personal.marketnote.common.utility.FormatValidator;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class NotificationSwaggerConfig {

    private static final String SERVICE_NAME = "알림";

    @Value("${server.origin}")
    private String serverOrigin;

    private final Environment environment;
    private final SwaggerOrigin swaggerOrigin;

    private static final List<String> TAGS_ORDER = List.of(
            "알림 API",
            "SSE 알림 스트림 API",
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

        String label = SwaggerEnvironment.resolveLabel(environment.getActiveProfiles());

        openApi.info(new Info()
                        .title(buildTitle(label))
                        .description(buildDescription(label))
                        .version("1.0"))
                .components(new Components()
                        .addSecuritySchemes("bearer", securityScheme))
                .addSecurityItem(new SecurityRequirement().addList("bearer"))
                .servers(List.of(
                        new Server().url(serverOrigin)
                ));

        return openApi;
    }

    private String buildTitle(String label) {
        String envPart = FormatValidator.hasNoValue(label) ? "" : label + " ";
        return "마켓노트 " + envPart + SERVICE_NAME + " API 서버";
    }

    private String buildDescription(String label) {
        String envPart = FormatValidator.hasNoValue(label) ? "" : label + " ";
        String header = "마켓노트 서비스 " + envPart + SERVICE_NAME + " API 서버입니다.";

        String links = Arrays.stream(SwaggerServiceSubdomain.values())
                .map(subdomain -> subdomain.markdownLink(swaggerOrigin.scheme(), swaggerOrigin.baseHost()))
                .collect(Collectors.joining("\n\n"));

        return """
                %s

                ### 서비스 목록
                %s
                """.formatted(header, links);
    }
}
