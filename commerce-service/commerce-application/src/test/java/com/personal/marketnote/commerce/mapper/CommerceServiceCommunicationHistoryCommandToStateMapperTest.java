package com.personal.marketnote.commerce.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationHistoryCreateState;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationSenderType;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationTargetType;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationType;
import com.personal.marketnote.commerce.port.in.command.servicecommunication.CommerceServiceCommunicationHistoryCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CommerceServiceCommunicationHistoryCommandToStateMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("커맨드의 모든 필드가 CreateState로 정확히 매핑된다")
    void mapToCreateState_allFields_mapsCorrectly() {
        // given
        JsonNode payloadJson = objectMapper.createObjectNode().put("key", "value");
        CommerceServiceCommunicationHistoryCommand command = CommerceServiceCommunicationHistoryCommand.builder()
                .targetType(CommerceServiceCommunicationTargetType.PRODUCT_INFO)
                .targetId("target-123")
                .communicationType(CommerceServiceCommunicationType.REQUEST)
                .sender(CommerceServiceCommunicationSenderType.COMMERCE)
                .exception("RuntimeException")
                .payload("payload text")
                .payloadJson(payloadJson)
                .build();

        // when
        CommerceServiceCommunicationHistoryCreateState state =
                CommerceServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getTargetType()).isEqualTo(CommerceServiceCommunicationTargetType.PRODUCT_INFO);
        assertThat(state.getTargetId()).isEqualTo("target-123");
        assertThat(state.getCommunicationType()).isEqualTo(CommerceServiceCommunicationType.REQUEST);
        assertThat(state.getSender()).isEqualTo(CommerceServiceCommunicationSenderType.COMMERCE);
        assertThat(state.getException()).isEqualTo("RuntimeException");
        assertThat(state.getPayload()).isEqualTo("payload text");
        assertThat(state.getPayloadJson()).isEqualTo(payloadJson);
    }

    @Test
    @DisplayName("커맨드의 선택 필드가 null이면 CreateState에도 null로 매핑된다")
    void mapToCreateState_nullOptionalFields_mapsNulls() {
        // given
        CommerceServiceCommunicationHistoryCommand command = CommerceServiceCommunicationHistoryCommand.builder()
                .targetType(CommerceServiceCommunicationTargetType.USER_POINT)
                .targetId(null)
                .communicationType(CommerceServiceCommunicationType.RESPONSE)
                .sender(CommerceServiceCommunicationSenderType.USER)
                .exception(null)
                .payload(null)
                .payloadJson(null)
                .build();

        // when
        CommerceServiceCommunicationHistoryCreateState state =
                CommerceServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getTargetType()).isEqualTo(CommerceServiceCommunicationTargetType.USER_POINT);
        assertThat(state.getCommunicationType()).isEqualTo(CommerceServiceCommunicationType.RESPONSE);
        assertThat(state.getSender()).isEqualTo(CommerceServiceCommunicationSenderType.USER);
        assertThat(state.getTargetId()).isNull();
        assertThat(state.getException()).isNull();
        assertThat(state.getPayload()).isNull();
        assertThat(state.getPayloadJson()).isNull();
    }

    @Test
    @DisplayName("각 TargetType이 정확히 매핑된다")
    void mapToCreateState_eachTargetType_mapsCorrectly() {
        for (CommerceServiceCommunicationTargetType targetType : CommerceServiceCommunicationTargetType.values()) {
            // given
            CommerceServiceCommunicationHistoryCommand command = CommerceServiceCommunicationHistoryCommand.builder()
                    .targetType(targetType)
                    .communicationType(CommerceServiceCommunicationType.REQUEST)
                    .sender(CommerceServiceCommunicationSenderType.COMMERCE)
                    .build();

            // when
            CommerceServiceCommunicationHistoryCreateState state =
                    CommerceServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

            // then
            assertThat(state.getTargetType()).isEqualTo(targetType);
        }
    }

    @Test
    @DisplayName("각 SenderType이 정확히 매핑된다")
    void mapToCreateState_eachSenderType_mapsCorrectly() {
        for (CommerceServiceCommunicationSenderType senderType : CommerceServiceCommunicationSenderType.values()) {
            // given
            CommerceServiceCommunicationHistoryCommand command = CommerceServiceCommunicationHistoryCommand.builder()
                    .targetType(CommerceServiceCommunicationTargetType.PRODUCT_INFO)
                    .communicationType(CommerceServiceCommunicationType.REQUEST)
                    .sender(senderType)
                    .build();

            // when
            CommerceServiceCommunicationHistoryCreateState state =
                    CommerceServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

            // then
            assertThat(state.getSender()).isEqualTo(senderType);
        }
    }

    @Test
    @DisplayName("각 CommunicationType이 정확히 매핑된다")
    void mapToCreateState_eachCommunicationType_mapsCorrectly() {
        for (CommerceServiceCommunicationType commType : CommerceServiceCommunicationType.values()) {
            // given
            CommerceServiceCommunicationHistoryCommand command = CommerceServiceCommunicationHistoryCommand.builder()
                    .targetType(CommerceServiceCommunicationTargetType.PRODUCT_INFO)
                    .communicationType(commType)
                    .sender(CommerceServiceCommunicationSenderType.COMMERCE)
                    .build();

            // when
            CommerceServiceCommunicationHistoryCreateState state =
                    CommerceServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

            // then
            assertThat(state.getCommunicationType()).isEqualTo(commType);
        }
    }

    @Test
    @DisplayName("복합 JSON payloadJson이 정확히 매핑된다")
    void mapToCreateState_complexJsonPayload_mapsCorrectly() {
        // given
        ObjectNode body = objectMapper.createObjectNode();
        body.put("productId", 100);
        body.put("quantity", 5);
        ObjectNode complexJson = objectMapper.createObjectNode();
        complexJson.put("method", "POST");
        complexJson.put("url", "http://api.example.com");
        complexJson.put("attempt", 3);
        complexJson.set("body", body);

        CommerceServiceCommunicationHistoryCommand command = CommerceServiceCommunicationHistoryCommand.builder()
                .targetType(CommerceServiceCommunicationTargetType.CART_PRODUCT)
                .communicationType(CommerceServiceCommunicationType.REQUEST)
                .sender(CommerceServiceCommunicationSenderType.COMMERCE)
                .payloadJson(complexJson)
                .build();

        // when
        CommerceServiceCommunicationHistoryCreateState state =
                CommerceServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getPayloadJson()).isEqualTo(complexJson);
        assertThat(state.getPayloadJson().get("method").asText()).isEqualTo("POST");
        assertThat(state.getPayloadJson().get("url").asText()).isEqualTo("http://api.example.com");
        assertThat(state.getPayloadJson().get("attempt").asInt()).isEqualTo(3);
        assertThat(state.getPayloadJson().get("body").get("productId").asInt()).isEqualTo(100);
        assertThat(state.getPayloadJson().get("body").get("quantity").asInt()).isEqualTo(5);
    }

    @Test
    @DisplayName("payload만 있고 payloadJson이 null이면 독립적으로 매핑된다")
    void mapToCreateState_payloadOnlyWithoutJson_mapsIndependently() {
        // given
        CommerceServiceCommunicationHistoryCommand command = CommerceServiceCommunicationHistoryCommand.builder()
                .targetType(CommerceServiceCommunicationTargetType.CART_PRODUCT)
                .communicationType(CommerceServiceCommunicationType.RESPONSE)
                .sender(CommerceServiceCommunicationSenderType.PRODUCT)
                .payload("text payload")
                .payloadJson(null)
                .build();

        // when
        CommerceServiceCommunicationHistoryCreateState state =
                CommerceServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getPayload()).isEqualTo("text payload");
        assertThat(state.getPayloadJson()).isNull();
    }

    @Test
    @DisplayName("payloadJson만 있고 payload가 null이면 독립적으로 매핑된다")
    void mapToCreateState_payloadJsonOnlyWithoutText_mapsIndependently() {
        // given
        JsonNode json = objectMapper.createObjectNode().put("status", 200);
        CommerceServiceCommunicationHistoryCommand command = CommerceServiceCommunicationHistoryCommand.builder()
                .targetType(CommerceServiceCommunicationTargetType.USER_POINT)
                .communicationType(CommerceServiceCommunicationType.RESPONSE)
                .sender(CommerceServiceCommunicationSenderType.USER)
                .payload(null)
                .payloadJson(json)
                .build();

        // when
        CommerceServiceCommunicationHistoryCreateState state =
                CommerceServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getPayload()).isNull();
        assertThat(state.getPayloadJson()).isEqualTo(json);
    }

    @Test
    @DisplayName("targetId가 빈 문자열이면 빈 문자열 그대로 매핑된다")
    void mapToCreateState_emptyTargetId_mapsAsEmptyString() {
        // given
        CommerceServiceCommunicationHistoryCommand command = CommerceServiceCommunicationHistoryCommand.builder()
                .targetType(CommerceServiceCommunicationTargetType.PRODUCT_INFO)
                .targetId("")
                .communicationType(CommerceServiceCommunicationType.REQUEST)
                .sender(CommerceServiceCommunicationSenderType.COMMERCE)
                .build();

        // when
        CommerceServiceCommunicationHistoryCreateState state =
                CommerceServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getTargetId()).isEmpty();
    }

    @Test
    @DisplayName("exception이 빈 문자열이면 빈 문자열 그대로 매핑된다")
    void mapToCreateState_emptyException_mapsAsEmptyString() {
        // given
        CommerceServiceCommunicationHistoryCommand command = CommerceServiceCommunicationHistoryCommand.builder()
                .targetType(CommerceServiceCommunicationTargetType.PRODUCT_INFO)
                .communicationType(CommerceServiceCommunicationType.REQUEST)
                .sender(CommerceServiceCommunicationSenderType.COMMERCE)
                .exception("")
                .build();

        // when
        CommerceServiceCommunicationHistoryCreateState state =
                CommerceServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getException()).isEmpty();
    }
}
