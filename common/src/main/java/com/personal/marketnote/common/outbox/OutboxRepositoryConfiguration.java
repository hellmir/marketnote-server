package com.personal.marketnote.common.outbox;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.personal.marketnote")
@EntityScan(basePackages = "com.personal.marketnote")
public class OutboxRepositoryConfiguration {
}
