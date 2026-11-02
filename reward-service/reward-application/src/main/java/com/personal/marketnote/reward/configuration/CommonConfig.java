package com.personal.marketnote.reward.configuration;

import com.personal.marketnote.common.configuration.security.OpaqueTokenIntrospectorConfig;
import com.personal.marketnote.common.configuration.security.OpaqueTokenIntrospectorProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
        OpaqueTokenIntrospectorConfig.class,
        OpaqueTokenIntrospectorProvider.class
})
public class CommonConfig {
}
