package com.personal.marketnote.common.outbox.adapter.in.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.common.outbox.OutboxResolutionAction;
import com.personal.marketnote.common.outbox.adapter.OutboxQueryService;
import com.personal.marketnote.common.outbox.adapter.OutboxResolveService;
import com.personal.marketnote.common.outbox.adapter.command.OutboxResolveCommand;
import com.personal.marketnote.common.outbox.adapter.command.OutboxResolveResult;
import com.personal.marketnote.common.outbox.adapter.in.web.request.ResolveOutboxRequest;
import com.personal.marketnote.common.outbox.adapter.in.web.response.OutboxEventResponse;
import com.personal.marketnote.common.outbox.adapter.in.web.response.OutboxTopicSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminOutboxController 테스트")
class AdminOutboxControllerTest {

    @InjectMocks
    private AdminOutboxController adminOutboxController;

    @Mock
    private OutboxQueryService outboxQueryService;

    @Mock
    private OutboxResolveService outboxResolveService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminOutboxController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("queryFailedEvents 요청 시 topic 파라미터를 전달하고 조회 결과를 반환한다")
    void queryFailedEvents_passesTopicParamAndReturnsEvents() throws Exception {
        // given
        String topic = "commerce.order.created";
        OutboxEventResponse response = new OutboxEventResponse(
                1L, "evt-1", topic, "pk-1", "OrderCreated", "commerce",
                2, 5, LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 1, 1, 1, 0), "last-error"
        );
        when(outboxQueryService.queryFailedEvents(topic)).thenReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/admin/outbox/failed")
                        .param("topic", topic))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].eventId").value("evt-1"))
                .andExpect(jsonPath("$.content[0].topic").value(topic))
                .andExpect(jsonPath("$.content[0].lastErrorMessage").value("last-error"))
                .andExpect(jsonPath("$.message").value("Outbox FAILED 이벤트 조회 완료"));

        verify(outboxQueryService).queryFailedEvents(topic);
    }

    @Test
    @DisplayName("resolveFailedEvent 요청 시 Command를 구성해 OutboxResolveService.resolve를 호출하고 결과를 응답한다")
    void resolveFailedEvent_delegatesToResolveService() throws Exception {
        // given
        ResolveOutboxRequest request = new ResolveOutboxRequest(
                100L, OutboxResolutionAction.RETRY, "재시도 요청"
        );
        OutboxResolveResult serviceResult = new OutboxResolveResult(
                "evt-100", "commerce.order.created", "RETRY", "재시도 요청", false
        );

        when(outboxResolveService.resolve(any(OutboxResolveCommand.class)))
                .thenReturn(serviceResult);

        // when & then
        mockMvc.perform(post("/api/v1/admin/outbox/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.id").value(100))
                .andExpect(jsonPath("$.content.eventId").value("evt-100"))
                .andExpect(jsonPath("$.content.topic").value("commerce.order.created"))
                .andExpect(jsonPath("$.content.resolution").value("RETRY"))
                .andExpect(jsonPath("$.content.reason").value("재시도 요청"))
                .andExpect(jsonPath("$.content.alreadyResolved").value(false))
                .andExpect(jsonPath("$.message").value("Outbox FAILED 이벤트 해결 완료"));

        ArgumentCaptor<OutboxResolveCommand> commandCaptor = ArgumentCaptor.forClass(OutboxResolveCommand.class);
        verify(outboxResolveService).resolve(commandCaptor.capture());
        OutboxResolveCommand captured = commandCaptor.getValue();
        assertThat(captured.id()).isEqualTo(100L);
        assertThat(captured.action()).isEqualTo(OutboxResolutionAction.RETRY);
        assertThat(captured.reason()).isEqualTo("재시도 요청");
    }

    @Test
    @DisplayName("resolveFailedEvent 응답에서 alreadyResolved 플래그를 그대로 전달한다")
    void resolveFailedEvent_propagatesAlreadyResolvedFlag() throws Exception {
        // given
        ResolveOutboxRequest request = new ResolveOutboxRequest(
                200L, OutboxResolutionAction.DISCARD, "이미 처리됨"
        );
        OutboxResolveResult serviceResult = new OutboxResolveResult(
                "evt-200", "commerce.payment.approved", "DISCARD", "이미 처리됨", true
        );
        when(outboxResolveService.resolve(any(OutboxResolveCommand.class)))
                .thenReturn(serviceResult);

        // when & then
        mockMvc.perform(post("/api/v1/admin/outbox/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.alreadyResolved").value(true))
                .andExpect(jsonPath("$.content.resolution").value("DISCARD"));
    }

    @Test
    @DisplayName("queryFailedSummary 요청 시 OutboxQueryService.queryFailedSummary의 결과를 반환한다")
    void queryFailedSummary_returnsSummaries() throws Exception {
        // given
        OutboxTopicSummaryResponse summary1 = new OutboxTopicSummaryResponse("commerce.order.created", 4L);
        OutboxTopicSummaryResponse summary2 = new OutboxTopicSummaryResponse("commerce.payment.approved", 9L);
        when(outboxQueryService.queryFailedSummary()).thenReturn(List.of(summary1, summary2));

        // when & then
        mockMvc.perform(get("/api/v1/admin/outbox/failed/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].topic").value("commerce.order.created"))
                .andExpect(jsonPath("$.content[0].failedCount").value(4))
                .andExpect(jsonPath("$.content[1].topic").value("commerce.payment.approved"))
                .andExpect(jsonPath("$.content[1].failedCount").value(9))
                .andExpect(jsonPath("$.message").value("Outbox FAILED 토픽별 요약 조회 완료"));

        verify(outboxQueryService).queryFailedSummary();
    }
}
