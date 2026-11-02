package com.personal.marketnote.user.configuration;

import com.personal.marketnote.common.application.aop.LoggingAspect;
import com.personal.marketnote.common.domain.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({LoggingAspect.class, GlobalExceptionHandler.class})
public class CommonConfig {
}
