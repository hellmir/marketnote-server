package com.personal.marketnote.commerce.adapter.out.persistence.vendorcommunication;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.marketnote.commerce.adapter.out.persistence.vendorcommunication.entity.CommerceVendorCommunicationHistoryJpaEntity;
import com.personal.marketnote.commerce.adapter.out.persistence.vendorcommunication.repository.CommerceVendorCommunicationHistoryJpaRepository;
import com.personal.marketnote.commerce.domain.vendorcommunication.CommerceVendorCommunicationHistory;
import com.personal.marketnote.commerce.port.out.vendorcommunication.SaveCommerceVendorCommunicationHistoryPort;
import com.personal.marketnote.common.adapter.out.PersistenceAdapter;
import com.personal.marketnote.common.utility.FormatValidator;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
public class CommerceVendorCommunicationHistoryPersistenceAdapter
        implements SaveCommerceVendorCommunicationHistoryPort {
    private final CommerceVendorCommunicationHistoryJpaRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public CommerceVendorCommunicationHistory save(CommerceVendorCommunicationHistory history) {
        JsonNode payloadJson = parseJsonNode(history.getPayloadJson());
        CommerceVendorCommunicationHistoryJpaEntity savedEntity = repository.save(
                CommerceVendorCommunicationHistoryJpaEntity.from(history, payloadJson)
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
