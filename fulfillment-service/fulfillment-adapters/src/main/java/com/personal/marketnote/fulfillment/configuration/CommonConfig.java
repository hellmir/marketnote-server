package com.personal.marketnote.fulfillment.configuration;

import com.personal.marketnote.common.application.aop.LoggingAspect;
import com.personal.marketnote.common.configuration.security.OpaqueTokenIntrospectorConfig;
import com.personal.marketnote.common.configuration.security.OpaqueTokenIntrospectorProvider;
import com.personal.marketnote.common.domain.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        LoggingAspect.class,
        GlobalExceptionHandler.class,
        OpaqueTokenIntrospectorConfig.class,
        OpaqueTokenIntrospectorProvider.class
})
public class CommonConfig {
}
