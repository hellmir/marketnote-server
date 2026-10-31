package com.personal.marketnote.common.utility.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ServiceCommunicationRecorderTest {

    private enum FakeTarget { ORDER, PAYMENT }
    private enum FakeType { REQUEST, RESPONSE }
    private enum FakeSender { SERVER, CLIENT }

    private ServiceCommunicationHistoryDispatcher<FakeTarget, FakeType, FakeSender> dispatcher;
    private ServiceCommunicationRecorder<FakeTarget, FakeType, FakeSender> recorder;
    private JsonNode payloadJson;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        dispatcher = mock(ServiceCommunicationHistoryDispatcher.class);
        recorder = new ServiceCommunicationRecorder<>(dispatcher);
        payloadJson = new ObjectMapper().createObjectNode().put("key", "value");
    }

    @Test
    @DisplayName("targetId 없이 호출하면 targetId=null, exception=null로 dispatch한다")
    void shouldDispatchWithoutTargetIdAndException() {
        recorder.record(FakeTarget.ORDER, FakeType.REQUEST, FakeSender.SERVER, "payload", payloadJson);

        verify(dispatcher).dispatch(
                eq(FakeTarget.ORDER), isNull(), eq(FakeType.REQUEST), eq(FakeSender.SERVER),
                isNull(), eq("payload"), eq(payloadJson)
        );
    }

    @Test
    @DisplayName("targetId 포함 호출 시 exception=null로 dispatch한다")
    void shouldDispatchWithTargetIdAndWithoutException() {
        recorder.record(FakeTarget.ORDER, FakeType.REQUEST, FakeSender.SERVER, "T-1", "payload", payloadJson);

        verify(dispatcher).dispatch(
                eq(FakeTarget.ORDER), eq("T-1"), eq(FakeType.REQUEST), eq(FakeSender.SERVER),
                isNull(), eq("payload"), eq(payloadJson)
        );
    }

    @Test
    @DisplayName("exception 포함 호출 시 targetId=null로 dispatch한다")
    void shouldDispatchWithExceptionAndWithoutTargetId() {
        recorder.record(
                FakeTarget.PAYMENT, FakeType.RESPONSE, FakeSender.CLIENT,
                "payload", payloadJson, "IOException"
        );

        verify(dispatcher).dispatch(
                eq(FakeTarget.PAYMENT), isNull(), eq(FakeType.RESPONSE), eq(FakeSender.CLIENT),
                eq("IOException"), eq("payload"), eq(payloadJson)
        );
    }

    @Test
    @DisplayName("targetId와 exception을 모두 포함한 호출이 그대로 dispatch된다")
    void shouldDispatchWithTargetIdAndException() {
        recorder.record(
                FakeTarget.PAYMENT, FakeType.RESPONSE, FakeSender.CLIENT,
                "T-2", "payload", payloadJson, "TimeoutException"
        );

        verify(dispatcher).dispatch(
                eq(FakeTarget.PAYMENT), eq("T-2"), eq(FakeType.RESPONSE), eq(FakeSender.CLIENT),
                eq("TimeoutException"), eq("payload"), eq(payloadJson)
        );
    }

    @Test
    @DisplayName("payloadJson이 null이어도 dispatcher에 null 그대로 전달한다")
    void shouldPassNullPayloadJsonAsIs() {
        recorder.record(FakeTarget.ORDER, FakeType.REQUEST, FakeSender.SERVER, "payload", null);

        verify(dispatcher).dispatch(
                eq(FakeTarget.ORDER), isNull(), eq(FakeType.REQUEST), eq(FakeSender.SERVER),
                isNull(), eq("payload"), isNull()
        );
    }
}
