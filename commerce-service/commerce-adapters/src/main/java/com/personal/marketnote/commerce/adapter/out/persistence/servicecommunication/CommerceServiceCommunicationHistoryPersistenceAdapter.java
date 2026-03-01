package com.personal.marketnote.commerce.adapter.out.persistence.servicecommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.servicecommunication.entity.CommerceServiceCommunicationHistoryJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.servicecommunication.repository.CommerceServiceCommunicationHistoryJpaRepository;
import com.personal.marketnote.commerce.domain.servicecommunication.CommerceServiceCommunicationHistory;
import com.personal.marketnote.commerce.port.out.servicecommunication.SaveCommerceServiceCommunicationHistoryPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class CommerceServiceCommunicationHistoryPersistenceAdapter
        implements SaveCommerceServiceCommunicationHistoryPort {
    private final CommerceServiceCommunicationHistoryJpaRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public CommerceServiceCommunicationHistory save(CommerceServiceCommunicationHistory history) {
        JsonNode payloadJson = parseJsonNode(history.getPayloadJson());
        CommerceServiceCommunicationHistoryJpaEntity savedEntity = repository.save(
                CommerceServiceCommunicationHistoryJpaEntity.from(history, payloadJson)
        );
        return savedEntity.toDomain();
    }

    private JsonNode parseJsonNode(String jsonString) {
        if (FormatValidator.hasNoValue(jsonString)) {
            return null;
        }
        try {
            return objectMapper.readTree(jsonString);
        } catch (Exception e) {
            return null;
        }
    }
}
