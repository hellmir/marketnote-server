package com.personal.marketnote.common.security.token.resolver;

import com.personal.marketnote.common.utility.FormatValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

import static com.personal.marketnote.common.security.token.utility.TokenConstant.AUTHENTICATION_SCHEME;

@Component
@Profile("!qa.test & !prod")
@RequiredArgsConstructor
public class JsonBearerTokenResolver implements BearerTokenResolver {
    @Override
    public String resolve(HttpServletRequest request) {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (FormatValidator.hasNoValue(authorization)) {
            return null;
        }

        if (!authorization.startsWith(AUTHENTICATION_SCHEME)) {
            return null;
        }
        return authorization.substring(7);
    }
}
