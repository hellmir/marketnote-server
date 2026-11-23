package com.personal.marketnote.common.utility.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VendorCommunicationRecorderTest {

    private enum FakeTarget {DELIVERY, SETTLEMENT}

    private enum FakeVendor {FASSTO, KCP}

    private enum FakeType {REQUEST, RESPONSE}

    private enum FakeSender {SERVER, VENDOR}

    private VendorCommunicationHistoryDispatcher<FakeTarget, FakeVendor, FakeType, FakeSender> dispatcher;
    private VendorCommunicationRecorder<FakeTarget, FakeVendor, FakeType, FakeSender> recorder;
    private JsonNode payloadJson;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        dispatcher = mock(VendorCommunicationHistoryDispatcher.class);
        recorder = new VendorCommunicationRecorder<>(dispatcher);
        payloadJson = new ObjectMapper().createObjectNode().put("key", "value");
    }

    @Test
    @DisplayName("targetId 없이 호출하면 targetId=null, exception=null, attemptCount=1로 dispatch한다")
    void shouldDispatchWithoutTargetIdAndException() {
        recorder.record(
                FakeTarget.DELIVERY, FakeType.REQUEST, FakeSender.SERVER,
                FakeVendor.FASSTO, "payload", payloadJson
        );

        verify(dispatcher).dispatch(
                eq(FakeTarget.DELIVERY), isNull(), eq(FakeVendor.FASSTO),
                eq(FakeType.REQUEST), eq(FakeSender.SERVER),
                isNull(), eq("payload"), eq(payloadJson), eq(1)
        );
    }

    @Test
    @DisplayName("targetId 포함 호출 시 exception=null, attemptCount=1로 dispatch한다")
    void shouldDispatchWithTargetIdAndWithoutException() {
        recorder.record(
                FakeTarget.DELIVERY, FakeType.REQUEST, FakeSender.SERVER,
                "T-1", FakeVendor.FASSTO, "payload", payloadJson
        );

        verify(dispatcher).dispatch(
                eq(FakeTarget.DELIVERY), eq("T-1"), eq(FakeVendor.FASSTO),
                eq(FakeType.REQUEST), eq(FakeSender.SERVER),
                isNull(), eq("payload"), eq(payloadJson), eq(1)
        );
    }

    @Test
    @DisplayName("exception 포함 호출 시 targetId=null, attemptCount=1로 dispatch한다")
    void shouldDispatchWithExceptionAndWithoutTargetId() {
        recorder.record(
                FakeTarget.SETTLEMENT, FakeType.RESPONSE, FakeSender.VENDOR,
                FakeVendor.KCP, "payload", payloadJson, "IOException"
        );

        verify(dispatcher).dispatch(
                eq(FakeTarget.SETTLEMENT), isNull(), eq(FakeVendor.KCP),
                eq(FakeType.RESPONSE), eq(FakeSender.VENDOR),
                eq("IOException"), eq("payload"), eq(payloadJson), eq(1)
        );
    }

    @Test
    @DisplayName("targetId와 exception을 모두 포함한 호출이 attemptCount=1로 dispatch된다")
    void shouldDispatchWithTargetIdAndException() {
        recorder.record(
                FakeTarget.SETTLEMENT, FakeType.RESPONSE, FakeSender.VENDOR,
                "T-2", FakeVendor.KCP, "payload", payloadJson, "TimeoutException"
        );

        verify(dispatcher).dispatch(
                eq(FakeTarget.SETTLEMENT), eq("T-2"), eq(FakeVendor.KCP),
                eq(FakeType.RESPONSE), eq(FakeSender.VENDOR),
                eq("TimeoutException"), eq("payload"), eq(payloadJson), eq(1)
        );
    }

    @Test
    @DisplayName("payloadJson이 null이어도 dispatcher에 null 그대로, attemptCount=1로 전달한다")
    void shouldPassNullPayloadJsonAsIs() {
        recorder.record(
                FakeTarget.DELIVERY, FakeType.REQUEST, FakeSender.SERVER,
                FakeVendor.FASSTO, "payload", null
        );

        verify(dispatcher).dispatch(
                eq(FakeTarget.DELIVERY), isNull(), eq(FakeVendor.FASSTO),
                eq(FakeType.REQUEST), eq(FakeSender.SERVER),
                isNull(), eq("payload"), isNull(), eq(1)
        );
    }

    @Test
    @DisplayName("targetId 없이 attemptCount 명시 호출 시 그대로 dispatch한다")
    void shouldDispatchWithAttemptCountWithoutTargetIdAndException() {
        recorder.record(
                FakeTarget.DELIVERY, FakeType.REQUEST, FakeSender.SERVER,
                FakeVendor.FASSTO, "payload", payloadJson, 3
        );

        verify(dispatcher).dispatch(
                eq(FakeTarget.DELIVERY), isNull(), eq(FakeVendor.FASSTO),
                eq(FakeType.REQUEST), eq(FakeSender.SERVER),
                isNull(), eq("payload"), eq(payloadJson), eq(3)
        );
    }

    @Test
    @DisplayName("targetId 포함 + attemptCount 명시 호출 시 그대로 dispatch한다")
    void shouldDispatchWithTargetIdAndAttemptCount() {
        recorder.record(
                FakeTarget.DELIVERY, FakeType.REQUEST, FakeSender.SERVER,
                "T-1", FakeVendor.FASSTO, "payload", payloadJson, 2
        );

        verify(dispatcher).dispatch(
                eq(FakeTarget.DELIVERY), eq("T-1"), eq(FakeVendor.FASSTO),
                eq(FakeType.REQUEST), eq(FakeSender.SERVER),
                isNull(), eq("payload"), eq(payloadJson), eq(2)
        );
    }

    @Test
    @DisplayName("exception + attemptCount 명시 호출 시 targetId=null로 dispatch한다")
    void shouldDispatchWithExceptionAndAttemptCount() {
        recorder.record(
                FakeTarget.SETTLEMENT, FakeType.RESPONSE, FakeSender.VENDOR,
                FakeVendor.KCP, "payload", payloadJson, "IOException", 4
        );

        verify(dispatcher).dispatch(
                eq(FakeTarget.SETTLEMENT), isNull(), eq(FakeVendor.KCP),
                eq(FakeType.RESPONSE), eq(FakeSender.VENDOR),
                eq("IOException"), eq("payload"), eq(payloadJson), eq(4)
        );
    }

    @Test
    @DisplayName("targetId + exception + attemptCount 모두 명시 호출이 그대로 dispatch된다")
    void shouldDispatchWithTargetIdExceptionAndAttemptCount() {
        recorder.record(
                FakeTarget.SETTLEMENT, FakeType.RESPONSE, FakeSender.VENDOR,
                "T-2", FakeVendor.KCP, "payload", payloadJson, "TimeoutException", 5
        );

        verify(dispatcher).dispatch(
                eq(FakeTarget.SETTLEMENT), eq("T-2"), eq(FakeVendor.KCP),
                eq(FakeType.RESPONSE), eq(FakeSender.VENDOR),
                eq("TimeoutException"), eq("payload"), eq(payloadJson), eq(5)
        );
    }

    @Test
    @DisplayName("attemptCount 명시 호출에서 payloadJson이 null이어도 dispatcher에 null 그대로 전달한다")
    void shouldPassNullPayloadJsonAsIsWithAttemptCount() {
        recorder.record(
                FakeTarget.DELIVERY, FakeType.REQUEST, FakeSender.SERVER,
                FakeVendor.FASSTO, "payload", null, 3
        );

        verify(dispatcher).dispatch(
                eq(FakeTarget.DELIVERY), isNull(), eq(FakeVendor.FASSTO),
                eq(FakeType.REQUEST), eq(FakeSender.SERVER),
                isNull(), eq("payload"), isNull(), eq(3)
        );
    }
}
