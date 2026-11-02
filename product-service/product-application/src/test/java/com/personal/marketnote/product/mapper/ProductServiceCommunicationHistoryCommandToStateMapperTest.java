package com.personal.marketnote.product.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationHistoryCreateState;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationSenderType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationTargetType;
import com.personal.marketnote.product.domain.servicecommunication.ProductServiceCommunicationType;
import com.personal.marketnote.product.port.in.command.servicecommunication.ProductServiceCommunicationHistoryCommand;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductServiceCommunicationHistoryCommandToStateMapperTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("커맨드의 모든 필드가 CreateState로 정확히 매핑된다")
    void mapToCreateState_allFields_mapsCorrectly() {
        // given
        JsonNode payloadJson = objectMapper.createObjectNode().put("key", "value");
        ProductServiceCommunicationHistoryCommand command = ProductServiceCommunicationHistoryCommand.builder()
                .targetType(ProductServiceCommunicationTargetType.INVENTORY)
                .targetId("target-123")
                .communicationType(ProductServiceCommunicationType.REQUEST)
                .sender(ProductServiceCommunicationSenderType.PRODUCT)
                .exception("RuntimeException")
                .payload("payload text")
                .payloadJson(payloadJson)
                .build();

        // when
        ProductServiceCommunicationHistoryCreateState state =
                ProductServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getTargetType()).isEqualTo(ProductServiceCommunicationTargetType.INVENTORY);
        assertThat(state.getTargetId()).isEqualTo("target-123");
        assertThat(state.getCommunicationType()).isEqualTo(ProductServiceCommunicationType.REQUEST);
        assertThat(state.getSender()).isEqualTo(ProductServiceCommunicationSenderType.PRODUCT);
        assertThat(state.getException()).isEqualTo("RuntimeException");
        assertThat(state.getPayload()).isEqualTo("payload text");
        assertThat(state.getPayloadJson()).isEqualTo(payloadJson);
    }

    @Test
    @DisplayName("커맨드의 선택 필드가 null이면 CreateState에도 null로 매핑된다")
    void mapToCreateState_nullOptionalFields_mapsNulls() {
        // given
        ProductServiceCommunicationHistoryCommand command = ProductServiceCommunicationHistoryCommand.builder()
                .targetType(ProductServiceCommunicationTargetType.PRODUCT_REVIEW_AGGREGATE)
                .targetId(null)
                .communicationType(ProductServiceCommunicationType.RESPONSE)
                .sender(ProductServiceCommunicationSenderType.COMMUNITY)
                .exception(null)
                .payload(null)
                .payloadJson(null)
                .build();

        // when
        ProductServiceCommunicationHistoryCreateState state =
                ProductServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getTargetType()).isEqualTo(ProductServiceCommunicationTargetType.PRODUCT_REVIEW_AGGREGATE);
        assertThat(state.getCommunicationType()).isEqualTo(ProductServiceCommunicationType.RESPONSE);
        assertThat(state.getSender()).isEqualTo(ProductServiceCommunicationSenderType.COMMUNITY);
        assertThat(state.getTargetId()).isNull();
        assertThat(state.getException()).isNull();
        assertThat(state.getPayload()).isNull();
        assertThat(state.getPayloadJson()).isNull();
    }

    @Test
    @DisplayName("각 TargetType이 정확히 매핑된다")
    void mapToCreateState_eachTargetType_mapsCorrectly() {
        for (ProductServiceCommunicationTargetType targetType : ProductServiceCommunicationTargetType.values()) {
            // given
            ProductServiceCommunicationHistoryCommand command = ProductServiceCommunicationHistoryCommand.builder()
                    .targetType(targetType)
                    .communicationType(ProductServiceCommunicationType.REQUEST)
                    .sender(ProductServiceCommunicationSenderType.PRODUCT)
                    .build();

            // when
            ProductServiceCommunicationHistoryCreateState state =
                    ProductServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

            // then
            assertThat(state.getTargetType()).isEqualTo(targetType);
        }
    }

    @Test
    @DisplayName("각 SenderType이 정확히 매핑된다")
    void mapToCreateState_eachSenderType_mapsCorrectly() {
        for (ProductServiceCommunicationSenderType senderType : ProductServiceCommunicationSenderType.values()) {
            // given
            ProductServiceCommunicationHistoryCommand command = ProductServiceCommunicationHistoryCommand.builder()
                    .targetType(ProductServiceCommunicationTargetType.INVENTORY)
                    .communicationType(ProductServiceCommunicationType.REQUEST)
                    .sender(senderType)
                    .build();

            // when
            ProductServiceCommunicationHistoryCreateState state =
                    ProductServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

            // then
            assertThat(state.getSender()).isEqualTo(senderType);
        }
    }

    @Test
    @DisplayName("각 CommunicationType이 정확히 매핑된다")
    void mapToCreateState_eachCommunicationType_mapsCorrectly() {
        for (ProductServiceCommunicationType commType : ProductServiceCommunicationType.values()) {
            // given
            ProductServiceCommunicationHistoryCommand command = ProductServiceCommunicationHistoryCommand.builder()
                    .targetType(ProductServiceCommunicationTargetType.INVENTORY)
                    .communicationType(commType)
                    .sender(ProductServiceCommunicationSenderType.PRODUCT)
                    .build();

            // when
            ProductServiceCommunicationHistoryCreateState state =
                    ProductServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

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

        ProductServiceCommunicationHistoryCommand command = ProductServiceCommunicationHistoryCommand.builder()
                .targetType(ProductServiceCommunicationTargetType.FULFILLMENT_GOODS)
                .communicationType(ProductServiceCommunicationType.REQUEST)
                .sender(ProductServiceCommunicationSenderType.PRODUCT)
                .payloadJson(complexJson)
                .build();

        // when
        ProductServiceCommunicationHistoryCreateState state =
                ProductServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

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
        ProductServiceCommunicationHistoryCommand command = ProductServiceCommunicationHistoryCommand.builder()
                .targetType(ProductServiceCommunicationTargetType.PRODUCT_IMAGE)
                .communicationType(ProductServiceCommunicationType.RESPONSE)
                .sender(ProductServiceCommunicationSenderType.FILE)
                .payload("text payload")
                .payloadJson(null)
                .build();

        // when
        ProductServiceCommunicationHistoryCreateState state =
                ProductServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getPayload()).isEqualTo("text payload");
        assertThat(state.getPayloadJson()).isNull();
    }

    @Test
    @DisplayName("payloadJson만 있고 payload가 null이면 독립적으로 매핑된다")
    void mapToCreateState_payloadJsonOnlyWithoutText_mapsIndependently() {
        // given
        JsonNode json = objectMapper.createObjectNode().put("status", 200);
        ProductServiceCommunicationHistoryCommand command = ProductServiceCommunicationHistoryCommand.builder()
                .targetType(ProductServiceCommunicationTargetType.PRODUCT_IMAGE)
                .communicationType(ProductServiceCommunicationType.RESPONSE)
                .sender(ProductServiceCommunicationSenderType.FILE)
                .payload(null)
                .payloadJson(json)
                .build();

        // when
        ProductServiceCommunicationHistoryCreateState state =
                ProductServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getPayload()).isNull();
        assertThat(state.getPayloadJson()).isEqualTo(json);
    }

    @Test
    @DisplayName("targetId가 빈 문자열이면 빈 문자열 그대로 매핑된다")
    void mapToCreateState_emptyTargetId_mapsAsEmptyString() {
        // given
        ProductServiceCommunicationHistoryCommand command = ProductServiceCommunicationHistoryCommand.builder()
                .targetType(ProductServiceCommunicationTargetType.INVENTORY)
                .targetId("")
                .communicationType(ProductServiceCommunicationType.REQUEST)
                .sender(ProductServiceCommunicationSenderType.PRODUCT)
                .build();

        // when
        ProductServiceCommunicationHistoryCreateState state =
                ProductServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getTargetId()).isEmpty();
    }

    @Test
    @DisplayName("exception이 빈 문자열이면 빈 문자열 그대로 매핑된다")
    void mapToCreateState_emptyException_mapsAsEmptyString() {
        // given
        ProductServiceCommunicationHistoryCommand command = ProductServiceCommunicationHistoryCommand.builder()
                .targetType(ProductServiceCommunicationTargetType.INVENTORY)
                .communicationType(ProductServiceCommunicationType.REQUEST)
                .sender(ProductServiceCommunicationSenderType.PRODUCT)
                .exception("")
                .build();

        // when
        ProductServiceCommunicationHistoryCreateState state =
                ProductServiceCommunicationHistoryCommandToStateMapper.mapToCreateState(command);

        // then
        assertThat(state.getException()).isEmpty();
    }
}
