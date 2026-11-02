package com.personal.marketnote.notification.adapter.in.web.sse.controller;

import com.personal.marketnote.common.utility.ElementExtractor;
import com.personal.marketnote.notification.adapter.in.web.sse.controller.apidocs.SubscribeSseStreamApiDocs;
import com.personal.marketnote.notification.adapter.in.web.sse.registry.SseConnectionRegistry;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "SSE 알림 스트림 API", description = "실시간 알림 SSE 스트림 API")
public class SseNotificationController {

    private final SseConnectionRegistry sseConnectionRegistry;
    private final long emitterTimeoutMs;

    public SseNotificationController(
            SseConnectionRegistry sseConnectionRegistry,
            @Value("${sse.emitter.timeout-ms:1800000}") long emitterTimeoutMs
    ) {
        this.sseConnectionRegistry = sseConnectionRegistry;
        this.emitterTimeoutMs = emitterTimeoutMs;
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @SubscribeSseStreamApiDocs
    public SseEmitter subscribe(
            @AuthenticationPrincipal OAuth2AuthenticatedPrincipal principal
    ) {
        Long userId = ElementExtractor.extractUserId(principal);
        return sseConnectionRegistry.register(userId, emitterTimeoutMs);
    }
}
