package com.personal.marketnote.common.configuration;

import com.personal.marketnote.common.security.hmac.HmacServiceAuthHeaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class HmacConfig {
    @Bean
    public HmacServiceAuthHeaderBuilder hmacServiceAuthHeaderBuilder(
            @Value("${spring.hmac.secret-key}") String hmacSecretKey,
            Clock clock) {
        return new HmacServiceAuthHeaderBuilder(hmacSecretKey, clock);
    }
}
