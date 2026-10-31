package com.personal.marketnote.common.adapter.in.web.kafka.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.adapter.in.web.kafka.request.ReprocessDltRequest;
import com.personal.marketnote.common.adapter.in.web.kafka.request.ResolveDltRequest;
import com.personal.marketnote.common.adapter.in.web.kafka.response.DltMessageResponse;
import com.personal.marketnote.common.adapter.in.web.kafka.response.DltTopicSummaryResponse;
import com.personal.marketnote.common.configuration.kafka.DltAuditLogger;
import com.personal.marketnote.common.configuration.kafka.DltQueryService;
import com.personal.marketnote.common.configuration.kafka.DltReprocessResult;
import com.personal.marketnote.common.configuration.kafka.DltReprocessService;
import com.personal.marketnote.common.configuration.kafka.DltResolutionAction;
import com.personal.marketnote.common.configuration.kafka.DltResolutionStatus;
import com.personal.marketnote.common.configuration.kafka.DltResolveCommand;
import com.personal.marketnote.common.configuration.kafka.DltResolveResult;
import com.personal.marketnote.common.configuration.kafka.DltResolveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminDltController 테스트")
class AdminDltControllerTest {

    private static final String ADMIN_USER_KEY = "test-admin";

    @InjectMocks
    private AdminDltController adminDltController;

    @Mock
    private DltReprocessService dltReprocessService;

    @Mock
    private DltResolveService dltResolveService;

    @Mock
    private DltQueryService dltQueryService;

    @Mock
    private DltAuditLogger dltAuditLogger;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminDltController)
                .setCustomArgumentResolvers(new FixedOAuth2PrincipalResolver(ADMIN_USER_KEY))
                .build();
    }

    @Test
    @DisplayName("reprocessDlt 요청 시 DltReprocessService.reprocess를 호출하고 원본 토픽을 전달한다")
    void reprocessDlt_delegatesToReprocessService() throws Exception {
        // given
        String originalTopic = "commerce.order.payment-completed";
        ReprocessDltRequest request = new ReprocessDltRequest(originalTopic);
        DltReprocessResult serviceResult = new DltReprocessResult(5, 1, 2);

        when(dltReprocessService.reprocess(originalTopic, ADMIN_USER_KEY))
                .thenReturn(serviceResult);

        // when & then
        mockMvc.perform(post("/api/v1/admin/kafka/dlt/reprocess")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.originalTopic").value(originalTopic))
                .andExpect(jsonPath("$.content.dltTopic").value(originalTopic + ".dlt"))
                .andExpect(jsonPath("$.content.reprocessedCount").value(5))
                .andExpect(jsonPath("$.content.failedCount").value(1))
                .andExpect(jsonPath("$.content.skippedCount").value(2))
                .andExpect(jsonPath("$.message").value("DLT 메시지 재처리 완료"));

        verify(dltReprocessService).reprocess(originalTopic, ADMIN_USER_KEY);
    }

    @Test
    @DisplayName("resolveDlt 요청 시 DltResolveService.resolve에 Command를 전달하고 해결 상태를 응답한다")
    void resolveDlt_delegatesToResolveService() throws Exception {
        // given
        String originalTopic = "commerce.payment.approved";
        ResolveDltRequest request = new ResolveDltRequest(
                originalTopic, 0, 42L, DltResolutionAction.RETRY, "수동 재시도"
        );
        DltResolveResult serviceResult = new DltResolveResult(DltResolutionStatus.RETRIED, false);

        when(dltResolveService.resolve(
                org.mockito.ArgumentMatchers.any(DltResolveCommand.class), eq(ADMIN_USER_KEY)
        )).thenReturn(serviceResult);

        // when & then
        mockMvc.perform(post("/api/v1/admin/kafka/dlt/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.originalTopic").value(originalTopic))
                .andExpect(jsonPath("$.content.partition").value(0))
                .andExpect(jsonPath("$.content.offset").value(42))
                .andExpect(jsonPath("$.content.resolution").value("RETRIED"))
                .andExpect(jsonPath("$.content.reason").value("수동 재시도"))
                .andExpect(jsonPath("$.content.alreadyResolved").value(false))
                .andExpect(jsonPath("$.message").value("DLT 메시지 해결 완료"));

        ArgumentCaptor<DltResolveCommand> commandCaptor = ArgumentCaptor.forClass(DltResolveCommand.class);
        verify(dltResolveService).resolve(commandCaptor.capture(), eq(ADMIN_USER_KEY));
        DltResolveCommand captured = commandCaptor.getValue();
        assertThat(captured.originalTopic()).isEqualTo(originalTopic);
        assertThat(captured.partition()).isEqualTo(0);
        assertThat(captured.offset()).isEqualTo(42L);
        assertThat(captured.action()).isEqualTo(DltResolutionAction.RETRY);
        assertThat(captured.reason()).isEqualTo("수동 재시도");
    }

    @Test
    @DisplayName("queryDltMessages 요청 시 limit 파라미터를 DltQueryService에 전달하고 감사 로그를 남긴다")
    void queryDltMessages_passesLimitAndLogsAudit() throws Exception {
        // given
        String originalTopic = "commerce.order.payment-completed";
        int limit = 50;
        DltMessageResponse messageResponse = new DltMessageResponse(
                originalTopic + ".dlt", 0, 1L, "key-1",
                originalTopic, "RuntimeException", "error-msg", 1700000000000L, "UNRESOLVED"
        );
        when(dltQueryService.queryDltMessages(originalTopic, limit))
                .thenReturn(List.of(messageResponse));

        // when & then
        mockMvc.perform(get("/api/v1/admin/kafka/dlt")
                        .param("original-topic", originalTopic)
                        .param("limit", String.valueOf(limit)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].dltTopic").value(originalTopic + ".dlt"))
                .andExpect(jsonPath("$.content[0].originalTopic").value(originalTopic))
                .andExpect(jsonPath("$.content[0].offset").value(1))
                .andExpect(jsonPath("$.content[0].resolution").value("UNRESOLVED"))
                .andExpect(jsonPath("$.message").value("DLT 메시지 조회 완료"));

        verify(dltQueryService).queryDltMessages(originalTopic, limit);
        verify(dltAuditLogger).logQuery(originalTopic, limit, ADMIN_USER_KEY);
    }

    @Test
    @DisplayName("queryDltTopicSummaries 요청 시 DltQueryService.queryAllDltTopicSummaries의 결과를 반환하고 감사 로그를 남긴다")
    void queryDltTopicSummaries_returnsSummariesAndLogsAudit() throws Exception {
        // given
        DltTopicSummaryResponse summary1 = new DltTopicSummaryResponse(
                "commerce.order.payment-completed", "commerce.order.payment-completed.dlt", 3L
        );
        DltTopicSummaryResponse summary2 = new DltTopicSummaryResponse(
                "commerce.payment.approved", "commerce.payment.approved.dlt", 7L
        );
        when(dltQueryService.queryAllDltTopicSummaries())
                .thenReturn(List.of(summary1, summary2));

        // when & then
        mockMvc.perform(get("/api/v1/admin/kafka/dlt/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].originalTopic").value("commerce.order.payment-completed"))
                .andExpect(jsonPath("$.content[0].messageCount").value(3))
                .andExpect(jsonPath("$.content[1].originalTopic").value("commerce.payment.approved"))
                .andExpect(jsonPath("$.content[1].messageCount").value(7))
                .andExpect(jsonPath("$.message").value("DLT 토픽 요약 조회 완료"));

        verify(dltQueryService).queryAllDltTopicSummaries();
        verify(dltAuditLogger).logSummaryQuery(ADMIN_USER_KEY);
    }

    /**
     * @AuthenticationPrincipal OAuth2AuthenticatedPrincipal 파라미터에
     * 고정 테스트 Principal 을 주입하는 커스텀 resolver.
     * SecurityContext 설정 없이 standaloneSetup 에서 Principal 을 결정적으로 제공한다.
     */
    private static final class FixedOAuth2PrincipalResolver implements HandlerMethodArgumentResolver {
        private final OAuth2AuthenticatedPrincipal principal;

        FixedOAuth2PrincipalResolver(String name) {
            this.principal = new DefaultOAuth2AuthenticatedPrincipal(
                    name, Map.of("sub", name), List.of()
            );
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && OAuth2AuthenticatedPrincipal.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter, ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest, WebDataBinderFactory binderFactory
        ) {
            return principal;
        }
    }
}
