package com.personal.marketnote.common.configuration.web;

import com.personal.marketnote.common.logging.BusinessIdMdcInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class BusinessIdMdcWebMvcConfig implements WebMvcConfigurer {

    private final BusinessIdMdcInterceptor businessIdMdcInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(businessIdMdcInterceptor)
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**");
    }
}
