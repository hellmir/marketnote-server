package com.personal.marketnote.fulfillment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.availability.AvailabilityChangeEvent;
import org.springframework.boot.availability.LivenessState;
import org.springframework.boot.availability.ReadinessState;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = HealthProbeIntegrationTest.TestConfig.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "management.health.redis.enabled=false",
        "management.health.kafka.enabled=false"
})
class HealthProbeIntegrationTest {

    @Configuration
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
                                    "com\\.personal\\.shop\\.common\\.configuration\\.security\\..*"
                            }
                    )
            }
    )
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationContext applicationContext;

    @BeforeEach
    void publishAvailabilityStates() {
        AvailabilityChangeEvent.publish(applicationContext, LivenessState.CORRECT);
        AvailabilityChangeEvent.publish(applicationContext, ReadinessState.ACCEPTING_TRAFFIC);
    }

    @Test
    @DisplayName("/actuator/health/liveness 응답이 200 OK이고 status는 UP이다")
    void livenessProbeReturnsUp() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("/actuator/health/readiness 응답이 200 OK이고 status는 UP이다")
    void readinessProbeReturnsUp() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("readiness 그룹 include 설정에 db, redis, kafka가 포함된다")
    void readinessGroupIncludesDbRedisKafka() {
        String include = environment.getProperty("management.endpoint.health.group.readiness.include");
        assertThat(include).isNotNull();
        assertThat(include).contains("db", "redis", "kafka");
    }

    @Test
    @DisplayName("liveness 그룹 include 설정에 livenessState가 포함된다")
    void livenessGroupIncludesLivenessState() {
        String include = environment.getProperty("management.endpoint.health.group.liveness.include");
        assertThat(include).isNotNull();
        assertThat(include).contains("livenessState");
    }

    @Test
    @DisplayName("management.endpoint.health.probes.enabled가 true로 설정된다")
    void healthProbesAreEnabled() {
        Boolean enabled = environment.getProperty("management.endpoint.health.probes.enabled", Boolean.class);
        assertThat(enabled).isTrue();
    }
}
