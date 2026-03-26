package com.personal.marketnote.common.saga;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.personal.marketnote.common.saga.repository")
@EntityScan(basePackages = "com.personal.marketnote.common.saga.entity")
public class SagaRepositoryConfiguration {
}
