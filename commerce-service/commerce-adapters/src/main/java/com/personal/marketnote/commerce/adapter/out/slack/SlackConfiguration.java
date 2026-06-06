package com.personal.marketnote.commerce.adapter.out.slack;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SlackProperties.class)
public class SlackConfiguration {
}
