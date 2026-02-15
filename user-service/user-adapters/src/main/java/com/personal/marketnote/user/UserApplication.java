package com.personal.marketnote.user;

import com.personal.marketnote.common.configuration.security.OpaqueTokenIntrospectorConfig;
import com.personal.marketnote.common.configuration.security.OpaqueTokenIntrospectorProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@SpringBootApplication(scanBasePackages = {
        "com.personal.marketnote"
})
@ComponentScan(
        basePackages = "com.personal.marketnote",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = OpaqueTokenIntrospectorConfig.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = OpaqueTokenIntrospectorProvider.class)
        }
)
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}
