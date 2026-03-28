package com.personal.marketnote.common.configuration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.security.hmac.HmacAuthenticationFilter;
import com.personal.marketnote.common.security.hmac.HmacHeaderConstants;
import com.personal.marketnote.common.security.hmac.HmacNonceValidator;
import com.personal.marketnote.common.security.token.resolver.JsonBearerTokenResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile({"qa.test", "prod"})
public class OpaqueTokenIntrospectorConfig {
    @Bean
    public HmacNonceValidator hmacNonceValidator(StringRedisTemplate stringRedisTemplate) {
        return new HmacNonceValidator(stringRedisTemplate);
    }

    @Bean
    public HmacAuthenticationFilter hmacAuthenticationFilter(
            @Value("${spring.hmac.secret-key}") String hmacSecretKey,
            HmacNonceValidator hmacNonceValidator,
            Clock clock) {
        return new HmacAuthenticationFilter(hmacSecretKey, hmacNonceValidator, clock);
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        return new JsonBearerTokenResolver();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return new AuthenticationDefaultEntryPoint(objectMapper);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   BearerTokenResolver resolver,
                                                   AuthenticationEntryPoint entryPoint,
                                                   HmacAuthenticationFilter hmacAuthenticationFilter) throws Exception {
        http.addFilterBefore(hmacAuthenticationFilter, BearerTokenAuthenticationFilter.class)
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(Customizer.withDefaults())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                        .cacheControl(Customizer.withDefaults()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/authentication/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/authentication/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/prometheus").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/sign-up").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users/sign-in").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/sign-out").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/authentication/access-token/refresh").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/terms").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/boards/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reviews/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/offerwalls/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/offerwalls/adiscope/callback").permitAll()
                        .requestMatchers("/api/v1/internal/**").hasAuthority("ROLE_SERVICE")
                        .anyRequest().authenticated())
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(entryPoint)
                        .bearerTokenResolver(resolver)
                        .opaqueToken(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${client.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOriginPatterns(origins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept",
                "X-Requested-With", "X-HTTP-Method-Override",
                HmacHeaderConstants.HEADER_SIGNATURE,
                HmacHeaderConstants.HEADER_TIMESTAMP,
                HmacHeaderConstants.HEADER_NONCE));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
