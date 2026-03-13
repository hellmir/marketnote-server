package com.personal.marketnote.file;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import software.amazon.awssdk.services.s3.S3Client;

@SpringBootTest(classes = FileApplicationContextTest.TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class FileApplicationContextTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            OAuth2ResourceServerAutoConfiguration.class,
            KafkaAutoConfiguration.class,
            BatchAutoConfiguration.class
    })
    @ComponentScan(
            basePackages = "com.personal.marketnote",
            excludeFilters = {
                    @ComponentScan.Filter(
                            type = FilterType.ANNOTATION,
                            classes = SpringBootApplication.class
                    ),
                    @ComponentScan.Filter(
                            type = FilterType.REGEX,
                            pattern = {
                                    "com\\.personal\\.shop\\.common\\.security\\..*",
                                    "com\\.personal\\.shop\\.common\\.configuration\\.security\\..*",
                                    "com\\.personal\\.shop\\.file\\.adapter\\.in\\.configuration\\.S3Config"
                            }
                    )
            }
    )
    static class TestConfig {

        @Bean
        public S3Client s3Client() {
            return Mockito.mock(S3Client.class);
        }
    }

    @Test
    @DisplayName("애플리케이션 컨텍스트가 정상적으로 로드된다")
    void contextLoads() {
    }
}
