package com.personal.marketnote.product.configuration;

import com.personal.marketnote.common.application.aop.LoggingAspect;
import com.personal.marketnote.common.configuration.security.OpaqueTokenIntrospectorConfig;
import com.personal.marketnote.common.configuration.security.OpaqueTokenIntrospectorProvider;
import com.personal.marketnote.common.domain.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@Import({
        LoggingAspect.class,
        GlobalExceptionHandler.class,
        OpaqueTokenIntrospectorConfig.class,
        OpaqueTokenIntrospectorProvider.class
})
public class CommonConfig {

    @Bean
    public Clock productClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
