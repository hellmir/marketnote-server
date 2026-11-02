package com.personal.marketnote.user.adapter.in.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.user.security.token.support.JsonTokenSupport;
import com.personal.marketnote.user.security.token.support.TokenSupport;
import com.personal.marketnote.user.utility.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("!qa.test & !prod")
public class SecurityConfig {
    @Bean
    public TokenSupport tokenSupport(ObjectMapper objectMapper) {
        return new JsonTokenSupport(objectMapper);
    }

    @Bean
    public JwtUtil jwtUtil(
            @Value("${spring.jwt.secret:dev-secret-change-me}") String jwtSecret,
            @Value("${spring.jwt.access-token.ttl:1800000}") Long accessTokenTtl,
            @Value("${spring.jwt.refresh-token.ttl:1209600000}") Long refreshTokenTtl
    ) {
        return new JwtUtil(jwtSecret, accessTokenTtl, refreshTokenTtl);
    }

    @Bean
    public SecurityFilterChain localSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
