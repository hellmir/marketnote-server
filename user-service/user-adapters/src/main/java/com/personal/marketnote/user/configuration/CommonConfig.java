package com.personal.marketnote.user.configuration;

import com.personal.marketnote.common.application.aop.LoggingAspect;
import com.personal.marketnote.common.domain.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@Import({LoggingAspect.class, GlobalExceptionHandler.class})
public class CommonConfig {

    @Bean
    public Clock userClock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
