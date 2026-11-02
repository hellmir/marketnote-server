package com.personal.marketnote.common.utility.methodoverride;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Component
@Order(1)
@Slf4j
public class HttpMethodOverrideFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String methodOverride = httpRequest.getHeader(HttpMethodOverrideConstant.HEADER_NAME);

        if (StringUtils.hasText(methodOverride)) {
            httpRequest = new MethodOverrideHttpServletRequestWrapper(httpRequest, methodOverride);
        }

        filterChain.doFilter(httpRequest, response);
    }
}
