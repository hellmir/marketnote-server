package com.personal.marketnote.fulfillment.configuration;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

// fulfillment-service는 application.yml에 shedlock.provider=jdbc 설정으로 JdbcTemplateLockProvider만 활성화 (#4911 정합).
// JdbcTemplateLockProvider는 내부적으로 DataSourceTransactionManager를 생성하므로 진짜 DataSource가 필요 → H2 in-memory 사용.
class FulfillmentLockProviderIntegrationTest {

    @Configuration
    static class TestDataSourceConfig {
        @Bean
        DataSource dataSource() {
            return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .build();
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }
    }

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withPropertyValues("shedlock.provider=jdbc")
        .withUserConfiguration(TestDataSourceConfig.class, FulfillmentSchedulerLockConfig.class);

    @Test
    @DisplayName("LockProvider 빈은 정확히 1개이고 JdbcTemplateLockProvider 인스턴스다")
    void lockProviderShouldBeJdbcTemplateLockProvider() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(LockProvider.class);
            assertThat(context.getBean(LockProvider.class)).isInstanceOf(JdbcTemplateLockProvider.class);
        });
    }
}
